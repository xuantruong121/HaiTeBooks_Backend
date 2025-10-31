package iuh.fit.haitebooks_backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        if (activeProfile.equalsIgnoreCase("dev")) {
            // 🌱 Cho phép tất cả khi dev
            config.setAllowedOriginPatterns(List.of("*"));
            config.setAllowCredentials(false);
            System.out.println("✅ [CORS] DEV mode — allow all origins");
        } else {
            // 🚀 PROD mode — chỉ cho phép domain FE thật
            config.setAllowedOrigins(List.of(frontendUrl));
            config.setAllowCredentials(true);
            System.out.println("✅ [CORS] PROD mode — only allow " + frontendUrl);
        }

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}