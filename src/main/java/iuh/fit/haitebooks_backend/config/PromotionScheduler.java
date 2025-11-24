package iuh.fit.haitebooks_backend.config;

import iuh.fit.haitebooks_backend.service.PromotionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task để tự động vô hiệu hóa các promotion đã hết hạn
 * Chạy mỗi ngày lúc 0h (midnight)
 */
@Component
public class PromotionScheduler {

    private static final Logger log = LoggerFactory.getLogger(PromotionScheduler.class);
    private final PromotionService promotionService;

    public PromotionScheduler(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    /**
     * Tự động vô hiệu hóa các promotion đã hết hạn
     * Chạy mỗi ngày lúc 0h (midnight) theo timezone Asia/Ho_Chi_Minh
     */
    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Ho_Chi_Minh")
    public void deactivateExpiredPromotions() {
        log.info("🔄 [SCHEDULER] Bắt đầu kiểm tra và vô hiệu hóa các promotion đã hết hạn...");
        try {
            promotionService.deactivateExpiredPromotions();
            log.info("✅ [SCHEDULER] Hoàn thành kiểm tra promotion đã hết hạn");
        } catch (Exception e) {
            log.error("❌ [SCHEDULER] Lỗi khi vô hiệu hóa promotion đã hết hạn: {}", e.getMessage(), e);
        }
    }
}

