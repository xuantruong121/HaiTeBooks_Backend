package iuh.fit.haitebooks_backend.ai.controller;

import iuh.fit.haitebooks_backend.ai.service.BookEmbeddingGenerator;
import iuh.fit.haitebooks_backend.ai.service.BookRecommendationService;
import iuh.fit.haitebooks_backend.ai.service.BookSearchService;
import iuh.fit.haitebooks_backend.dtos.response.BookResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
@Validated
public class AIController {

    private final BookSearchService bookSearchService;
    private final BookRecommendationService bookRecommendationService;
    private final BookEmbeddingGenerator embeddingGenerator;

    public AIController(BookSearchService bookSearchService, 
                       BookRecommendationService bookRecommendationService,
                       BookEmbeddingGenerator embeddingGenerator) {
        this.bookSearchService = bookSearchService;
        this.bookRecommendationService = bookRecommendationService;
        this.embeddingGenerator = embeddingGenerator;
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
     * Tạo embedding cho tất cả sách chưa có embedding
     * Chạy trong background thread để không block request
     * @return Thông báo xác nhận
     */
    @PostMapping("/generate-embeddings")
    public ResponseEntity<Map<String, String>> generateEmbeddings() {
        // Chạy trong background thread để không block request
        new Thread(() -> {
            try {
                embeddingGenerator.generateAllEmbeddings();
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi sinh embedding: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "🚀 Đã bắt đầu sinh embedding cho các sách chưa có. Xem log để theo dõi tiến trình.");
        response.put("status", "processing");
        return ResponseEntity.accepted().body(response);
    }
}
