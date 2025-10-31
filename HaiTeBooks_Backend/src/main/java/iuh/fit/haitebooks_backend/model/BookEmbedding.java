package iuh.fit.haitebooks_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "book_embeddings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BookEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "book_id", nullable = false)
    @JsonIgnore
    private Book book;

    @Lob
    @Column(name = "embedding_json", columnDefinition = "TEXT")
    private String embeddingJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Convert JSON string vector thành List<Double>
     */
    public List<Double> getEmbeddingVector() {
        if (embeddingJson == null || embeddingJson.isEmpty()) return List.of();
        return Arrays.stream(embeddingJson.replace("[", "").replace("]", "").split(","))
                .map(String::trim)
                .map(Double::parseDouble)
                .toList();
    }

    /**
     * Lưu List<Double> vào JSON string
     */
    public void setEmbeddingVector(List<Double> vector) {
        this.embeddingJson = vector.toString();
    }
}
