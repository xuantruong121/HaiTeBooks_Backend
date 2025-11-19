package iuh.fit.haitebooks_backend.ai.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Bean
    public Dotenv dotenv() {
        return Dotenv.configure()
                .ignoreIfMissing()
                .load();
    }

    @Bean
    public String cohereApiKey(Dotenv dotenv) {
        // ✅ Ưu tiên lấy từ .env, nếu không có thì lấy từ environment variables
        String key = dotenv.get("COHERE_API_KEY", System.getenv("COHERE_API_KEY"));
        if (key == null || key.isEmpty()) {
            throw new IllegalStateException("❌ COHERE_API_KEY not found in .env or environment variables");
        }
        return key;
    }

    @Bean
    public String cohereEmbeddingModel(Dotenv dotenv) {
        // Model đa ngôn ngữ mạnh nhất hiện tại
        return dotenv.get("COHERE_EMBEDDING_MODEL", 
                System.getenv("COHERE_EMBEDDING_MODEL") != null 
                    ? System.getenv("COHERE_EMBEDDING_MODEL") 
                    : "embed-multilingual-v3.0");
    }
}