package iuh.fit.haitebooks_backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Cấu hình DataSource với timezone Việt Nam
 */
@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);
    private static final String VIETNAM_TIMEZONE = "Asia/Ho_Chi_Minh";

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        // ✅ Tự động thêm serverTimezone vào URL nếu chưa có
        String urlWithTimezone = ensureTimezoneInUrl(datasourceUrl);
        
        log.info("✅ Đã cấu hình DataSource với timezone: {} (UTC+7)", VIETNAM_TIMEZONE);
        log.debug("Database URL: {}", urlWithTimezone.replaceAll("password=[^&@]*", "password=***"));
        
        return DataSourceBuilder.create()
                .url(urlWithTimezone)
                .username(datasourceUsername)
                .password(datasourcePassword)
                .build();
    }

    /**
     * Đảm bảo URL có tham số serverTimezone
     * Nếu URL đã có tham số, append thêm; nếu chưa có, thêm vào
     */
    private String ensureTimezoneInUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        // Kiểm tra xem URL đã có serverTimezone chưa
        if (url.contains("serverTimezone=")) {
            // Nếu đã có, kiểm tra xem có đúng timezone chưa
            if (url.contains("serverTimezone=" + VIETNAM_TIMEZONE) || 
                url.contains("serverTimezone=%2F" + VIETNAM_TIMEZONE.replace("/", "%2F"))) {
                log.debug("URL đã có serverTimezone đúng");
                return url;
            } else {
                // Nếu có nhưng sai timezone, thay thế
                log.warn("⚠️ URL có serverTimezone khác, sẽ thay thế bằng {}", VIETNAM_TIMEZONE);
                return url.replaceAll("serverTimezone=[^&]*", "serverTimezone=" + VIETNAM_TIMEZONE);
            }
        }

        // Nếu chưa có serverTimezone, thêm vào
        String separator = url.contains("?") ? "&" : "?";
        String timezoneParams = separator + "serverTimezone=" + VIETNAM_TIMEZONE + 
                               "&useLegacyDatetimeCode=false&useTimezone=true";
        
        return url + timezoneParams;
    }
}

