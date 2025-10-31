package iuh.fit.haitebooks_backend.ai.controller;

import iuh.fit.haitebooks_backend.ai.service.BookEmbeddingGenerator;
import iuh.fit.haitebooks_backend.ai.service.BookRecommendationService;
import iuh.fit.haitebooks_backend.ai.service.BookSearchService;
import iuh.fit.haitebooks_backend.model.Book;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    private final BookSearchService bookSearchService;
    private final BookRecommendationService bookRecommendationService;
    private final BookEmbeddingGenerator embeddingGenerator;

    public AIController(BookSearchService bookSearchService, BookRecommendationService bookRecommendationService, BookEmbeddingGenerator embeddingGenerator) {
        this.bookSearchService = bookSearchService;
        this.bookRecommendationService = bookRecommendationService;
        this.embeddingGenerator = embeddingGenerator;
    }

    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam String q) {
        return bookSearchService.smartSearch(q);
    }

    @GetMapping("/recommend/{bookId}")
    public List<Book> recommend(@PathVariable Long bookId) {
        return bookRecommendationService.recommendSimilarBooks(bookId);
    }

    @PostMapping("/generate-embeddings")
    public String generateEmbeddings() {
        new Thread(embeddingGenerator::generateAllEmbeddings).start();
        return "🚀 Đã bắt đầu sinh embedding cho các sách chưa có. Xem log để theo dõi tiến trình.";
    }
}
