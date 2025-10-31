package iuh.fit.haitebooks_backend.ai.service;

import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.BookEmbedding;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import iuh.fit.haitebooks_backend.repository.BookEmbeddingRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookSearchService {

    private final BookRepository bookRepository;
    private final BookEmbeddingRepository embeddingRepository;
    private final AIService aiService;

    public BookSearchService(BookRepository bookRepository,
                             BookEmbeddingRepository embeddingRepository,
                             AIService aiService) {
        this.bookRepository = bookRepository;
        this.embeddingRepository = embeddingRepository;
        this.aiService = aiService;
    }

    /**
     * Semantic search tối ưu: batch load embeddings, tính cosine similarity
     */
    public List<Book> smartSearch(String query) {
        List<Double> queryVector = aiService.generateEmbedding(query);
        if (queryVector.isEmpty()) return List.of();

        // 1. Load tất cả sách và embedding vào bộ nhớ
        List<Book> allBooks = bookRepository.findAll();
        if (allBooks.isEmpty()) return List.of();

        // Map Book -> embedding vector
        Map<Book, List<Double>> bookVectors = new HashMap<>();
        List<BookEmbedding> allEmbeddings = embeddingRepository.findAll();

        // Build map BookId -> BookEmbedding
        Map<Long, BookEmbedding> embeddingMap = allEmbeddings.stream()
                .filter(be -> be.getBook() != null)
                .collect(Collectors.toMap(be -> be.getBook().getId(), be -> be));

        for (Book book : allBooks) {
            BookEmbedding embedding = embeddingMap.get(book.getId());
            if (embedding != null && embedding.getEmbeddingVector() != null && !embedding.getEmbeddingVector().isEmpty()) {
                bookVectors.put(book, embedding.getEmbeddingVector());
            } else {
                // Nếu chưa có embedding thì tạo và lưu luôn
                String content = book.getTitle() + " " + Optional.ofNullable(book.getDescription()).orElse("");
                List<Double> vector = aiService.generateEmbedding(content);
                if (!vector.isEmpty()) {
                    BookEmbedding newEmbedding = new BookEmbedding();
                    newEmbedding.setBook(book);
                    newEmbedding.setEmbeddingVector(vector);
                    embeddingRepository.save(newEmbedding);
                    bookVectors.put(book, vector);
                }
            }
        }

        // 2. Tính cosine similarity với query
        Map<Book, Double> similarityMap = new HashMap<>();
        for (Map.Entry<Book, List<Double>> entry : bookVectors.entrySet()) {
            double score = cosineSimilarity(queryVector, entry.getValue());
            similarityMap.put(entry.getKey(), score);
        }

        // 3. Trả về top 10 sách liên quan nhất
        return similarityMap.entrySet().stream()
                .sorted(Map.Entry.<Book, Double>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        if (v1.size() != v2.size()) return 0.0;
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            dot += v1.get(i) * v2.get(i);
            normA += v1.get(i) * v1.get(i);
            normB += v2.get(i) * v2.get(i);
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-10);
    }
}
