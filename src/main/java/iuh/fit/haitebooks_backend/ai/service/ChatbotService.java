package iuh.fit.haitebooks_backend.ai.service;

import iuh.fit.haitebooks_backend.dtos.response.BookResponse;
import iuh.fit.haitebooks_backend.mapper.BookMapper;
import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    @Value("${COHERE_API_KEY}")
    private String cohereApiKey;

    private static final String CHAT_API_URL = "https://api.cohere.ai/v1/chat";
    private static final String EMBED_API_URL = "https://api.cohere.ai/v1/embed";

    private final RestTemplate restTemplate = new RestTemplate();
    private final BookRepository bookRepository;
    private final CohereEmbeddingService embeddingService;

    // System prompt cho chatbot
    private static final String SYSTEM_PROMPT = """
        Bạn là trợ lý AI thân thiện của cửa hàng sách HaiTeBooks. 
        Nhiệm vụ của bạn là:
        1. Trả lời câu hỏi về sách, tác giả, thể loại
        2. Gợi ý sách phù hợp với nhu cầu khách hàng
        3. Hỗ trợ thông tin về đơn hàng, thanh toán, giao hàng
        4. Trả lời bằng tiếng Việt một cách tự nhiên và thân thiện
        
        Nếu bạn không biết câu trả lời, hãy thành thật nói rằng bạn không chắc chắn và đề nghị khách hàng liên hệ bộ phận hỗ trợ.
        """;

    public ChatbotService(BookRepository bookRepository, CohereEmbeddingService embeddingService) {
        this.bookRepository = bookRepository;
        this.embeddingService = embeddingService;
    }

    /**
     * Xử lý tin nhắn từ khách hàng và trả lời
     */
    public Map<String, Object> chat(String userMessage, String conversationId) {
        log.info("💬 Nhận tin nhắn từ user: {}", userMessage);

        try {
            // 1. Tìm kiếm sách liên quan (RAG)
            List<Book> relevantBooks = findRelevantBooks(userMessage);
            log.info("📚 Tìm thấy {} sách liên quan", relevantBooks.size());

            // 2. Tạo context từ thông tin sách
            String context = buildContextFromBooks(relevantBooks);

            // 3. Gọi Cohere Chat API
            String aiResponse = callCohereChatAPI(userMessage, context);

            // 4. Trích xuất tên sách được đề xuất từ response
            List<String> suggestedBooks = extractBookNames(aiResponse, relevantBooks);

            // 5. Tạo sources (danh sách sách được tham khảo)
            List<String> sources = relevantBooks.stream()
                    .limit(3) // Chỉ lấy 3 sách đầu tiên
                    .map(Book::getTitle)
                    .collect(Collectors.toList());

            // 6. Tạo response
            Map<String, Object> response = new HashMap<>();
            response.put("response", aiResponse);
            response.put("suggestedBooks", suggestedBooks);
            response.put("sources", sources);
            response.put("conversationId", conversationId != null ? conversationId : UUID.randomUUID().toString());

            log.info("✅ Đã trả lời tin nhắn thành công");
            return response;

        } catch (Exception e) {
            log.error("❌ Lỗi khi xử lý chat: {}", e.getMessage(), e);
            
            // Fallback response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("response", "Xin lỗi, tôi đang gặp sự cố kỹ thuật. Vui lòng thử lại sau hoặc liên hệ bộ phận hỗ trợ.");
            errorResponse.put("suggestedBooks", List.of());
            errorResponse.put("sources", List.of());
            errorResponse.put("conversationId", conversationId != null ? conversationId : UUID.randomUUID().toString());
            return errorResponse;
        }
    }

    /**
     * Tìm kiếm sách liên quan dựa trên tin nhắn của user (RAG)
     */
    private List<Book> findRelevantBooks(String userMessage) {
        try {
            // Tìm kiếm đơn giản bằng keyword matching
            List<Book> allBooks = bookRepository.findAll();
            
            if (allBooks.isEmpty()) {
                return List.of();
            }

            // Tìm kiếm theo từ khóa trong title, author, description
            String lowerMessage = userMessage.toLowerCase();
            List<Book> relevantBooks = allBooks.stream()
                    .filter(book -> {
                        String title = book.getTitle() != null ? book.getTitle().toLowerCase() : "";
                        String author = book.getAuthor() != null ? book.getAuthor().toLowerCase() : "";
                        String description = book.getDescription() != null ? book.getDescription().toLowerCase() : "";
                        
                        // Kiểm tra từ khóa phổ biến về sách
                        return title.contains(lowerMessage) || 
                               author.contains(lowerMessage) ||
                               description.contains(lowerMessage) ||
                               lowerMessage.contains(title) ||
                               lowerMessage.contains(author);
                    })
                    .limit(5) // Giới hạn 5 sách
                    .collect(Collectors.toList());

            // Nếu không tìm thấy, trả về sách phổ biến (có nhiều stock)
            if (relevantBooks.isEmpty()) {
                relevantBooks = allBooks.stream()
                        .sorted((a, b) -> Integer.compare(b.getStock(), a.getStock()))
                        .limit(3)
                        .collect(Collectors.toList());
            }

            return relevantBooks;

        } catch (Exception e) {
            log.error("❌ Lỗi khi tìm kiếm sách: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Xây dựng context từ danh sách sách để đưa vào prompt
     */
    private String buildContextFromBooks(List<Book> books) {
        if (books.isEmpty()) {
            return "Hiện tại cửa hàng có nhiều sách hay. Bạn có thể hỏi về bất kỳ cuốn sách nào.";
        }

        StringBuilder context = new StringBuilder("Thông tin về các cuốn sách trong cửa hàng:\n\n");
        
        for (Book book : books) {
            context.append(String.format(
                "- Tên sách: %s\n" +
                "  Tác giả: %s\n" +
                "  Giá: %.0f VNĐ\n" +
                "  Mô tả: %s\n" +
                "  Tồn kho: %d cuốn\n\n",
                book.getTitle(),
                book.getAuthor(),
                book.getPrice(),
                book.getDescription() != null && book.getDescription().length() > 200 
                    ? book.getDescription().substring(0, 200) + "..." 
                    : book.getDescription(),
                book.getStock()
            ));
        }

        return context.toString();
    }

    /**
     * Gọi Cohere Chat API
     */
    private String callCohereChatAPI(String userMessage, String context) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + cohereApiKey);
            headers.set("Cohere-Version", "2022-12-06");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Xây dựng prompt với context
            String fullPrompt = SYSTEM_PROMPT + "\n\n" + 
                               "Thông tin về sách trong cửa hàng:\n" + context + "\n\n" +
                               "Câu hỏi của khách hàng: " + userMessage;

            JSONObject body = new JSONObject();
            // Sử dụng command-r-plus (hoặc command-r nếu không có)
            body.put("model", "command-r-plus");
            
            body.put("message", userMessage);
            
            // Kết hợp system prompt và context vào preamble
            String fullPreamble = SYSTEM_PROMPT;
            if (!context.isEmpty()) {
                fullPreamble += "\n\n" + context;
            }
            body.put("preamble", fullPreamble);

            body.put("temperature", 0.7);
            body.put("max_tokens", 1000);
            body.put("stream", false);

            HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    CHAT_API_URL, HttpMethod.POST, request, String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject json = new JSONObject(response.getBody());
                
                // Cohere Chat API trả về text trong field "text"
                if (json.has("text")) {
                    String text = json.getString("text");
                    log.info("✅ Nhận response từ Cohere: {}", 
                        text.length() > 100 ? text.substring(0, 100) + "..." : text);
                    return text;
                } else {
                    log.warn("⚠️ Response không có field 'text': {}", json.toString());
                    return "Xin lỗi, tôi không thể tạo câu trả lời. Vui lòng thử lại sau.";
                }
            } else {
                log.error("⚠️ Lỗi API: {} - {}", response.getStatusCode(), response.getBody());
                
                // Thử fallback với model đơn giản hơn
                if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    return tryFallbackChat(userMessage, context);
                }
                
                return "Xin lỗi, tôi không thể xử lý câu hỏi này ngay bây giờ. Vui lòng thử lại sau.";
            }

        } catch (Exception e) {
            log.error("❌ Lỗi khi gọi Cohere Chat API: {}", e.getMessage(), e);
            return "Xin lỗi, có lỗi xảy ra khi xử lý câu hỏi của bạn. Vui lòng thử lại sau.";
        }
    }

    /**
     * Trích xuất tên sách được đề xuất từ response
     */
    private List<String> extractBookNames(String response, List<Book> relevantBooks) {
        List<String> suggested = new ArrayList<>();
        
        // Kiểm tra xem response có đề cập đến sách nào không
        for (Book book : relevantBooks) {
            if (response.toLowerCase().contains(book.getTitle().toLowerCase())) {
                suggested.add(book.getTitle());
            }
        }

        return suggested.stream().distinct().limit(3).collect(Collectors.toList());
    }

    /**
     * Fallback chat với model đơn giản hơn nếu command-r-plus không khả dụng
     */
    private String tryFallbackChat(String userMessage, String context) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + cohereApiKey);
            headers.set("Cohere-Version", "2022-12-06");
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject body = new JSONObject();
            body.put("model", "command-r"); // Model đơn giản hơn
            body.put("message", userMessage);
            body.put("preamble", SYSTEM_PROMPT + "\n\n" + context);
            body.put("temperature", 0.7);
            body.put("max_tokens", 800);

            HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    CHAT_API_URL, HttpMethod.POST, request, String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject json = new JSONObject(response.getBody());
                return json.has("text") ? json.getString("text") : 
                       "Xin lỗi, tôi không thể tạo câu trả lời. Vui lòng thử lại sau.";
            }
        } catch (Exception e) {
            log.error("❌ Lỗi khi thử fallback chat: {}", e.getMessage());
        }
        
        return "Xin lỗi, hệ thống đang gặp sự cố. Vui lòng liên hệ bộ phận hỗ trợ.";
    }
}

