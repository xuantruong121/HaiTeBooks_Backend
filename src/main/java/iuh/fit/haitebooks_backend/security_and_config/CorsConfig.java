package iuh.fit.haitebooks_backend.security_and_config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        if ("dev".equalsIgnoreCase(activeProfile)) {
            config.setAllowedOriginPatterns(List.of(
                    "http://localhost:*",
                    "http://127.0.0.1:*",
                    "http://192.168.*.*:*"
            ));
            log.info("✅ [CORS] DEV mode — allow localhost & LAN");
        } else if ("prod".equalsIgnoreCase(activeProfile)) {
            config.setAllowedOriginPatterns(List.of(
                    frontendUrl,
                    "http://localhost:5173",
                    "http://192.168.100.156:5173",
                    "http://localhost:3001",
                    "http://192.168.100.156:3001"
            ));
            log.info("✅ [CORS] PROD mode — User App (5173) + Web Admin (3001)");
        } else {
            config.setAllowedOriginPatterns(List.of("*"));
            config.setAllowCredentials(false);
            log.warn("⚠️ [CORS] Unknown profile — allow all");
        }

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
