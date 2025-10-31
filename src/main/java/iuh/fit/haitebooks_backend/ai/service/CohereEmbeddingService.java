package iuh.fit.haitebooks_backend.ai.service;

import jakarta.annotation.PostConstruct;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class CohereEmbeddingService {

    @Value("${COHERE_API_KEY}")
    private String cohereApiKey;

    private static final String API_URL = "https://api.cohere.ai/v1/embed";

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void checkEndpoint() {
        System.out.println("✅ Using Cohere endpoint: " + API_URL);
    }

    public List<Double> generateEmbedding(String text) {
        int maxRetries = 3;
        int retryDelayMs = 2000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("🔄 Gọi Cohere API lần " + attempt + " cho text: \"" +
                        (text.length() > 60 ? text.substring(0, 60) + "..." : text) + "\"");

                // Header
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + cohereApiKey);
                headers.set("Cohere-Version", "2022-12-06");
                headers.setContentType(MediaType.APPLICATION_JSON);

                // Body
                JSONObject body = new JSONObject();
                body.put("model", "embed-multilingual-v3.0");
                body.put("input_type", "search_document");
                body.put("texts", new JSONArray().put(text));


                HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

                // Gửi request
                ResponseEntity<String> response = restTemplate.exchange(
                        API_URL, HttpMethod.POST, request, String.class
                );

                if (response.getStatusCode() == HttpStatus.OK) {
                    JSONObject json = new JSONObject(response.getBody());
                    JSONArray embeddingsArray = json.getJSONArray("embeddings").getJSONArray(0);

                    List<Double> embedding = new ArrayList<>();
                    for (int i = 0; i < embeddingsArray.length(); i++) {
                        embedding.add(embeddingsArray.getDouble(i));
                    }

                    System.out.println("✅ Embedding sinh thành công (" + embedding.size() + " chiều).");
                    return embedding;

                } else {
                    System.err.println("⚠️ Lỗi API: " + response.getStatusCode());
                    System.err.println("📦 Nội dung phản hồi: " + response.getBody());
                }

            } catch (Exception e) {
                System.err.println("❌ Lỗi khi gọi Cohere API (lần " + attempt + "): " + e.getMessage());
            }

            if (attempt < maxRetries) {
                try {
                    System.out.println("⏳ Đợi " + retryDelayMs + "ms trước khi thử lại...");
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException ignored) {}
            }
        }

        System.err.println("🚫 Không thể sinh embedding sau " + maxRetries + " lần thử.");
        return List.of();
    }
}
