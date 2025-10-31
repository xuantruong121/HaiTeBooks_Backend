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
        String key = dotenv.get("COHERE_API_KEY");
        if (key == null || key.isEmpty()) {
            throw new IllegalStateException("❌ COHERE_API_KEY not found in .env");
        }
        return key;
    }

    @Bean
    public String cohereEmbeddingModel(Dotenv dotenv) {
        // Model đa ngôn ngữ mạnh nhất hiện tại
        return dotenv.get("COHERE_EMBEDDING_MODEL", "embed-multilingual-v3.0");
    }
}
