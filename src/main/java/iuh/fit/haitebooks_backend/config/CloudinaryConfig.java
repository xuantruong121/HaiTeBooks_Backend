package iuh.fit.haitebooks_backend.config;

import com.cloudinary.Cloudinary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryConfig.class);

    @Value("${cloudinary.cloud.name}")
    private String cloudName;

    @Value("${cloudinary.api.key}")
    private String apiKey;

    @Value("${cloudinary.api.secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        // Validate cloud_name
        if (cloudName == null || cloudName.isEmpty() || cloudName.equals("root")) {
            log.error("❌ Lỗi: Cloudinary cloud_name không hợp lệ: '{}'. Vui lòng kiểm tra biến môi trường CLOUDINARY_CLOUD_NAME", cloudName);
            throw new IllegalStateException("Cloudinary cloud_name không hợp lệ. Vui lòng cấu hình CLOUDINARY_CLOUD_NAME trong file .env");
        }

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);

        log.info("✅ Đã cấu hình Cloudinary với cloud_name: {}", cloudName);
        log.debug("Cloudinary API Key: {}...", apiKey != null && apiKey.length() > 5 ? apiKey.substring(0, 5) : "N/A");
        return new Cloudinary(config);
    }
}

