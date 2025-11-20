package iuh.fit.haitebooks_backend.ai.service;

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
}

