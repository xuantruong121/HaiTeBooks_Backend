package iuh.fit.haitebooks_backend.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Cấu hình timezone mặc định cho ứng dụng là múi giờ Việt Nam (UTC+7)
 */
@Configuration
public class TimezoneConfig {

    private static final Logger log = LoggerFactory.getLogger(TimezoneConfig.class);
    private static final String VIETNAM_TIMEZONE = "Asia/Ho_Chi_Minh";

    @PostConstruct
    public void init() {
        // Set timezone mặc định cho JVM
        TimeZone.setDefault(TimeZone.getTimeZone(VIETNAM_TIMEZONE));
        log.info("✅ Đã cấu hình timezone mặc định: {} (UTC+7)", VIETNAM_TIMEZONE);
    }
}

