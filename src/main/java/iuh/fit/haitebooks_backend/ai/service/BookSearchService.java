package iuh.fit.haitebooks_backend.ai.service;

import iuh.fit.haitebooks_backend.dtos.response.BookResponse;
import iuh.fit.haitebooks_backend.mapper.BookMapper;
import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.BookEmbedding;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import iuh.fit.haitebooks_backend.repository.BookEmbeddingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookSearchService {

    private static final Logger log = LoggerFactory.getLogger(BookSearchService.class);
    private static final int DEFAULT_LIMIT = 10;
    private static final double MIN_SIMILARITY_THRESHOLD = 0.3; // Ngưỡng tối thiểu để trả về kết quả

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
     * @param query Câu truy vấn tìm kiếm
     * @param limit Số lượng kết quả tối đa (mặc định 10)
     * @return Danh sách sách được sắp xếp theo độ liên quan
     */
    @Transactional(readOnly = true)
    public List<BookResponse> smartSearch(String query, Integer limit) {
        if (query == null || query.trim().isEmpty()) {
            log.warn("⚠️ Query rỗng, trả về danh sách trống");
            return List.of();
        }

        int resultLimit = (limit != null && limit > 0) ? limit : DEFAULT_LIMIT;
        log.info("🔍 Bắt đầu tìm kiếm semantic với query: '{}', limit: {}", query, resultLimit);

        try {
            // 1. Tạo embedding cho query
            List<Double> queryVector = aiService.generateEmbedding(query.trim());
            if (queryVector.isEmpty()) {
                log.warn("⚠️ Không thể tạo embedding cho query: '{}'", query);
                return List.of();
            }

            // 2. Load tất cả sách và embedding vào bộ nhớ
            List<Book> allBooks = bookRepository.findAll();
            if (allBooks.isEmpty()) {
                log.info("📚 Không có sách nào trong database");
                return List.of();
            }

            // 3. Map Book -> embedding vector
            Map<Book, List<Double>> bookVectors = new HashMap<>();
            List<BookEmbedding> allEmbeddings = embeddingRepository.findAll();

            // Build map BookId -> BookEmbedding
            Map<Long, BookEmbedding> embeddingMap = allEmbeddings.stream()
                    .filter(be -> be.getBook() != null)
                    .collect(Collectors.toMap(be -> be.getBook().getId(), be -> be));

            int missingEmbeddings = 0;
            for (Book book : allBooks) {
                BookEmbedding embedding = embeddingMap.get(book.getId());
                if (embedding != null && embedding.getEmbeddingVector() != null && !embedding.getEmbeddingVector().isEmpty()) {
                    bookVectors.put(book, embedding.getEmbeddingVector());
                } else {
                    missingEmbeddings++;
                    log.debug("📝 Sách '{}' chưa có embedding, sẽ bỏ qua trong lần tìm kiếm này", book.getTitle());
                }
            }

            if (missingEmbeddings > 0) {
                log.info("ℹ️ Có {} sách chưa có embedding, đã bỏ qua", missingEmbeddings);
            }

            if (bookVectors.isEmpty()) {
                log.warn("⚠️ Không có sách nào có embedding để tìm kiếm");
                return List.of();
            }

            // 4. Tính cosine similarity với query
            Map<Book, Double> similarityMap = new HashMap<>();
            for (Map.Entry<Book, List<Double>> entry : bookVectors.entrySet()) {
                double score = cosineSimilarity(queryVector, entry.getValue());
                if (score >= MIN_SIMILARITY_THRESHOLD) {
                    similarityMap.put(entry.getKey(), score);
                }
            }

            // 5. Sắp xếp và lấy top kết quả
            List<Book> topBooks = similarityMap.entrySet().stream()
                    .sorted(Map.Entry.<Book, Double>comparingByValue().reversed())
                    .limit(resultLimit)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // 6. Map sang BookResponse và đảm bảo category được load
            List<BookResponse> results = topBooks.stream()
                    .map(book -> {
                        // Đảm bảo category được load trong transaction
                        if (book.getCategory() != null) {
                            book.getCategory().getName();
                        }
                        return BookMapper.toBookResponse(book);
                    })
                    .collect(Collectors.toList());

            log.info("✅ Tìm thấy {} kết quả cho query: '{}'", results.size(), query);
            return results;

        } catch (Exception e) {
            log.error("❌ Lỗi khi thực hiện semantic search: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Overload method với limit mặc định
     */
    @Transactional(readOnly = true)
    public List<BookResponse> smartSearch(String query) {
        return smartSearch(query, DEFAULT_LIMIT);
    }

    /**
     * Tính cosine similarity giữa 2 vector
     */
    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        if (v1 == null || v2 == null || v1.size() != v2.size()) {
            return 0.0;
        }

        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            double a = v1.get(i);
            double b = v2.get(i);
            dot += a * b;
            normA += a * a;
            normB += b * b;
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        if (denominator == 0.0) {
            return 0.0;
        }

        return dot / (denominator + 1e-10); // Thêm epsilon để tránh chia cho 0
    }
}
