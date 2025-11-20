package iuh.fit.haitebooks_backend.ai.service;

import iuh.fit.haitebooks_backend.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingAsyncService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingAsyncService.class);

    private final BookEmbeddingGenerator embeddingGenerator;

    public EmbeddingAsyncService(BookEmbeddingGenerator embeddingGenerator) {
        this.embeddingGenerator = embeddingGenerator;
    }

    /**
     * Method async để tạo embedding trong background
     * Sử dụng executor "embeddingTaskExecutor" được cấu hình trong AsyncConfig
     */
    @Async("embeddingTaskExecutor")
    public void generateEmbeddingsAsync() {
        try {
            log.info("🔄 Bắt đầu xử lý async: tạo embedding cho các sách");
            embeddingGenerator.generateAllEmbeddings();
            log.info("✅ Hoàn tất xử lý async: đã tạo embedding cho tất cả sách");
        } catch (Exception e) {
            log.error("❌ Lỗi khi sinh embedding trong async thread: {}", e.getMessage(), e);
        }
    }

    /**
     * Tạo embedding cho một cuốn sách cụ thể trong background thread
     * @param book Sách cần tạo embedding
     */
    @Async("embeddingTaskExecutor")
    public void generateEmbeddingForBookAsync(Book book) {
        try {
            log.info("🔄 Bắt đầu tạo embedding cho sách mới: '{}' (ID: {})", book.getTitle(), book.getId());
            boolean success = embeddingGenerator.generateEmbeddingForBook(book);
            if (success) {
                log.info("✅ Đã tạo embedding thành công cho sách: '{}' (ID: {})", book.getTitle(), book.getId());
            } else {
                log.warn("⚠️ Không thể tạo embedding cho sách: '{}' (ID: {}). Sẽ thử lại sau.", 
                        book.getTitle(), book.getId());
            }
        } catch (Exception e) {
            log.error("❌ Lỗi khi tạo embedding cho sách '{}' (ID: {}): {}", 
                    book.getTitle(), book.getId(), e.getMessage(), e);
        }
    }
}

