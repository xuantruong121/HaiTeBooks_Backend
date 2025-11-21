package iuh.fit.haitebooks_backend.ai.controller;

import iuh.fit.haitebooks_backend.ai.service.BookEmbeddingGenerator;
import iuh.fit.haitebooks_backend.ai.service.BookRecommendationService;
import iuh.fit.haitebooks_backend.ai.service.BookSearchService;
import iuh.fit.haitebooks_backend.ai.service.ChatbotService;
import iuh.fit.haitebooks_backend.ai.service.EmbeddingAsyncService;
import iuh.fit.haitebooks_backend.dtos.request.ChatRequest;
import iuh.fit.haitebooks_backend.dtos.response.BookResponse;
import iuh.fit.haitebooks_backend.dtos.response.ChatResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Validated
public class AIController {

    private static final Logger log = LoggerFactory.getLogger(AIController.class);

    private final BookSearchService bookSearchService;
    private final BookRecommendationService bookRecommendationService;
    private final EmbeddingAsyncService embeddingAsyncService;
    private final ChatbotService chatbotService;
    private final iuh.fit.haitebooks_backend.service.UserService userService;

    public AIController(BookSearchService bookSearchService, 
                       BookRecommendationService bookRecommendationService,
                       EmbeddingAsyncService embeddingAsyncService,
                       ChatbotService chatbotService,
                       iuh.fit.haitebooks_backend.service.UserService userService) {
        this.bookSearchService = bookSearchService;
        this.bookRecommendationService = bookRecommendationService;
        this.embeddingAsyncService = embeddingAsyncService;
        this.chatbotService = chatbotService;
        this.userService = userService;
    }

    /**
     * Tìm kiếm sách thông minh bằng semantic search
     * @param q Câu truy vấn tìm kiếm
     * @param limit Số lượng kết quả tối đa (mặc định 10, tối đa 50)
     * @return Danh sách sách được sắp xếp theo độ liên quan
     */
    @GetMapping("/search")
    public ResponseEntity<List<BookResponse>> searchBooks(
            @RequestParam @NotBlank(message = "Query không được để trống") String q,
            @RequestParam(required = false) @Min(value = 1, message = "Limit phải lớn hơn 0") Integer limit) {
        
        // Giới hạn limit tối đa để tránh quá tải
        int resultLimit = (limit != null && limit > 0) ? Math.min(limit, 50) : 10;
        
        List<BookResponse> results = bookSearchService.smartSearch(q, resultLimit);
        return ResponseEntity.ok(results);
    }

    /**
     * Gợi ý sách tương tự dựa trên embedding similarity
     * @param bookId ID của sách cần tìm sách tương tự
     * @param limit Số lượng sách gợi ý tối đa (mặc định 5, tối đa 20)
     * @return Danh sách sách tương tự được sắp xếp theo độ tương đồng
     */
    @GetMapping("/recommend/{bookId}")
    public ResponseEntity<List<BookResponse>> recommend(
            @PathVariable @NotNull(message = "BookId không được để trống") Long bookId,
            @RequestParam(required = false) @Min(value = 1, message = "Limit phải lớn hơn 0") Integer limit) {
        
        // Giới hạn limit tối đa
        int resultLimit = (limit != null && limit > 0) ? Math.min(limit, 20) : 5;
        
        List<BookResponse> results = bookRecommendationService.recommendSimilarBooks(bookId, resultLimit);
        return ResponseEntity.ok(results);
    }

    /**
     * Hybrid Recommendation: Gợi ý sách cho user hiện tại
     * Kết hợp Content-Based (embedding) + Collaborative Filtering (hành vi người dùng)
     * Không cần train model - sử dụng dữ liệu hiện có
     * 
     * @param userDetails User hiện tại từ authentication (tự động lấy từ JWT token)
     * @param userId ID của user (optional - nếu không có thì dùng user từ authentication)
     * @param limit Số lượng sách gợi ý tối đa (mặc định 10, tối đa 50)
     * @return Danh sách sách được gợi ý kết hợp từ nhiều phương pháp
     */
    @GetMapping("/recommend-for-user")
    public ResponseEntity<List<BookResponse>> recommendForUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @Min(value = 1, message = "Limit phải lớn hơn 0") Integer limit) {
        
        // Giới hạn limit tối đa
        int resultLimit = (limit != null && limit > 0) ? Math.min(limit, 50) : 10;
        
        // Lấy userId từ authentication nếu không có trong request
        Long targetUserId = userId;
        if (targetUserId == null && userDetails != null) {
            try {
                // Lấy user từ username trong token
                var userResponse = userService.getByUsername(userDetails.getUsername());
                targetUserId = userResponse.getId();
                log.info("🔐 Lấy userId từ authentication: {}", targetUserId);
            } catch (Exception e) {
                log.error("❌ Không thể lấy userId từ authentication: {}", e.getMessage());
                return ResponseEntity.badRequest().build();
            }
        }
        
        if (targetUserId == null) {
            log.warn("⚠️ Không có userId để gợi ý");
            return ResponseEntity.badRequest().build();
        }
        
        List<BookResponse> results = bookRecommendationService.recommendForUser(targetUserId, resultLimit);
        return ResponseEntity.ok(results);
    }

    /**
     * Tạo embedding cho tất cả sách chưa có embedding
     * Chạy trong background thread để không block request
     * @return Thông báo xác nhận
     */
    @PostMapping("/generate-embeddings")
    public ResponseEntity<Map<String, String>> generateEmbeddings() {
        log.info("📥 Nhận yêu cầu tạo embedding cho tất cả sách");
        
        // Chạy async để không block request
        embeddingAsyncService.generateEmbeddingsAsync();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "🚀 Đã bắt đầu sinh embedding cho các sách chưa có. Xem log để theo dõi tiến trình.");
        response.put("status", "processing");
        return ResponseEntity.accepted().body(response);
    }

    /**
     * Chatbot hỗ trợ khách hàng
     * Sử dụng RAG (Retrieval-Augmented Generation) với dữ liệu sách và đơn hàng
     * 
     * @param request ChatRequest chứa message và conversationId (optional)
     * @param userDetails User hiện tại từ authentication (optional - nếu đã đăng nhập)
     * @return ChatResponse với câu trả lời từ AI và danh sách sách được đề xuất
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @RequestBody @jakarta.validation.Valid ChatRequest request,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        log.info("💬 Nhận yêu cầu chat: {}", request.getMessage());
        
        try {
            // Lấy userId từ authentication nếu có
            Long userId = null;
            if (userDetails != null) {
                try {
                    var userResponse = userService.getByUsername(userDetails.getUsername());
                    userId = userResponse.getId();
                    log.info("🔐 Lấy userId từ authentication: {}", userId);
                } catch (Exception e) {
                    log.warn("⚠️ Không thể lấy userId từ authentication: {}", e.getMessage());
                }
            }
            
            Map<String, Object> result = chatbotService.chat(
                request.getMessage(), 
                request.getConversationId(),
                userId
            );
            
            ChatResponse response = new ChatResponse(
                (String) result.get("response"),
                (List<String>) result.get("suggestedBooks"),
                (List<String>) result.get("sources"),
                (String) result.get("conversationId")
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Lỗi khi xử lý chat: {}", e.getMessage(), e);
            ChatResponse errorResponse = new ChatResponse(
                "Xin lỗi, có lỗi xảy ra. Vui lòng thử lại sau.",
                List.of(),
                List.of(),
                request.getConversationId() != null ? request.getConversationId() : ""
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
