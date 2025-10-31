package iuh.fit.haitebooks_backend.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.BookEmbedding;
import iuh.fit.haitebooks_backend.repository.BookEmbeddingRepository;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookEmbeddingGenerator {

    private static final Logger log = LoggerFactory.getLogger(BookEmbeddingGenerator.class);

    private final BookRepository bookRepository;
    private final BookEmbeddingRepository embeddingRepository;
    private final AIService aiService;
    private final ObjectMapper mapper = new ObjectMapper();

    public BookEmbeddingGenerator(BookRepository bookRepository,
                                  BookEmbeddingRepository embeddingRepository,
                                  AIService aiService) {
        this.bookRepository = bookRepository;
        this.embeddingRepository = embeddingRepository;
        this.aiService = aiService;
    }

    public void generateAllEmbeddings() {
        log.info("🚀 Bắt đầu sinh embedding cho các sách chưa có...");
        List<Book> allBooks = bookRepository.findAll();
        int createdCount = 0;

        for (Book book : allBooks) {
            try {
                if (embeddingRepository.findByBookId(book.getId()).isPresent()) {
                    log.info("⏭️ Bỏ qua '{}': đã có embedding", book.getTitle());
                    continue;
                }

                String text = (book.getTitle() != null ? book.getTitle() : "") + " " +
                        (book.getDescription() != null ? book.getDescription() : "");

                List<Double> embedding = aiService.generateEmbedding(text);

                if (embedding.isEmpty()) {
                    log.warn("⚠️ Không tạo được embedding cho '{}'", book.getTitle());
                    continue;
                }

                String embeddingJson = mapper.writeValueAsString(embedding);

                BookEmbedding bookEmbedding = new BookEmbedding();
                bookEmbedding.setBook(book);
                bookEmbedding.setEmbeddingJson(embeddingJson);
                embeddingRepository.save(bookEmbedding);

                createdCount++;
                log.info("✅ Đã tạo embedding cho sách: {}", book.getTitle());

                Thread.sleep(1000); // Nghỉ giữa các request

            } catch (Exception e) {
                log.error("❌ Lỗi khi xử lý sách '{}': {}", book.getTitle(), e.getMessage());
            }
        }

        log.info("🎯 Hoàn tất! Đã sinh embedding cho {} sách mới.", createdCount);
    }
}
