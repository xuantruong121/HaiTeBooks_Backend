package iuh.fit.haitebooks_backend.ai.service;

import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.BookEmbedding;
import iuh.fit.haitebooks_backend.repository.BookEmbeddingRepository;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookEmbeddingGenerator {

    private static final Logger log = LoggerFactory.getLogger(BookEmbeddingGenerator.class);
    private static final long DELAY_BETWEEN_REQUESTS_MS = 1000; // 1 giây giữa các request để tránh rate limit

    private final BookRepository bookRepository;
    private final BookEmbeddingRepository embeddingRepository;
    private final AIService aiService;

    public BookEmbeddingGenerator(BookRepository bookRepository,
                                  BookEmbeddingRepository embeddingRepository,
                                  AIService aiService) {
        this.bookRepository = bookRepository;
        this.embeddingRepository = embeddingRepository;
        this.aiService = aiService;
    }

    /**
     * Tạo embedding cho tất cả sách chưa có embedding
     * Không dùng @Transactional ở đây vì transaction quá dài (có thể mất vài phút)
     * Thay vào đó, commit từng embedding một để đảm bảo dữ liệu được lưu ngay
     */
    public void generateAllEmbeddings() {
        log.info("🚀 Bắt đầu sinh embedding cho các sách chưa có...");
        
        try {
            List<Book> allBooks = bookRepository.findAll();
            int totalBooks = allBooks.size();
            int createdCount = 0;
            int skippedCount = 0;
            int errorCount = 0;

            log.info("📚 Tổng số sách trong database: {}", totalBooks);

            for (int i = 0; i < allBooks.size(); i++) {
                Book book = allBooks.get(i);
                
                try {
                    // Kiểm tra xem đã có embedding chưa
                    if (embeddingRepository.findByBookId(book.getId()).isPresent()) {
                        skippedCount++;
                        if ((i + 1) % 10 == 0) {
                            log.debug("⏭️ Đã kiểm tra {}/{} sách, bỏ qua '{}': đã có embedding", 
                                    i + 1, totalBooks, book.getTitle());
                        }
                        continue;
                    }

                    // Tạo text content từ title và description
                    String text = buildBookText(book);
                    if (text.trim().isEmpty()) {
                        log.warn("⚠️ Sách '{}' không có nội dung để tạo embedding", book.getTitle());
                        errorCount++;
                        continue;
                    }

                    // Tạo embedding
                    log.info("🔄 [{}/{}] Đang tạo embedding cho: '{}'", i + 1, totalBooks, book.getTitle());
                    List<Double> embedding = aiService.generateEmbedding(text);

                    if (embedding.isEmpty()) {
                        log.warn("⚠️ Không tạo được embedding cho '{}'", book.getTitle());
                        errorCount++;
                        continue;
                    }

                    // Lưu embedding vào database (commit ngay lập tức)
                    saveEmbedding(book, embedding);

                    createdCount++;
                    log.info("✅ [{}/{}] Đã tạo embedding cho: '{}' ({} chiều)", 
                            i + 1, totalBooks, book.getTitle(), embedding.size());

                    // Nghỉ giữa các request để tránh rate limit
                    if (i < allBooks.size() - 1) {
                        Thread.sleep(DELAY_BETWEEN_REQUESTS_MS);
                    }

                } catch (InterruptedException e) {
                    log.error("❌ Thread bị gián đoạn");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    errorCount++;
                    log.error("❌ Lỗi khi xử lý sách '{}' (ID: {}): {}", 
                            book.getTitle(), book.getId(), e.getMessage(), e);
                    // Tiếp tục với sách tiếp theo thay vì dừng lại
                }
            }

            log.info("🎯 Hoàn tất! Tổng kết:");
            log.info("   - Tổng số sách: {}", totalBooks);
            log.info("   - Đã tạo mới: {}", createdCount);
            log.info("   - Đã có sẵn (bỏ qua): {}", skippedCount);
            log.info("   - Lỗi: {}", errorCount);

        } catch (Exception e) {
            log.error("❌ Lỗi nghiêm trọng khi sinh embedding: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi sinh embedding", e);
        }
    }

    /**
     * Lưu embedding vào database với transaction riêng để commit ngay lập tức
     */
    @Transactional
    private void saveEmbedding(Book book, List<Double> embedding) {
        BookEmbedding bookEmbedding = new BookEmbedding();
        bookEmbedding.setBook(book);
        bookEmbedding.setEmbeddingVector(embedding);
        embeddingRepository.save(bookEmbedding);
    }

    /**
     * Xây dựng text content từ book để tạo embedding
     */
    private String buildBookText(Book book) {
        StringBuilder text = new StringBuilder();
        
        if (book.getTitle() != null && !book.getTitle().trim().isEmpty()) {
            text.append(book.getTitle().trim());
        }
        
        if (book.getDescription() != null && !book.getDescription().trim().isEmpty()) {
            if (text.length() > 0) {
                text.append(" ");
            }
            text.append(book.getDescription().trim());
        }
        
        if (book.getAuthor() != null && !book.getAuthor().trim().isEmpty()) {
            if (text.length() > 0) {
                text.append(" ");
            }
            text.append(book.getAuthor().trim());
        }
        
        return text.toString();
    }
}
