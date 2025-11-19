package iuh.fit.haitebooks_backend.ai.service;

import iuh.fit.haitebooks_backend.dtos.response.BookResponse;
import iuh.fit.haitebooks_backend.mapper.BookMapper;
import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.BookEmbedding;
import iuh.fit.haitebooks_backend.repository.BookEmbeddingRepository;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(BookRecommendationService.class);
    private static final int DEFAULT_LIMIT = 5;
    private static final double MIN_SIMILARITY_THRESHOLD = 0.3;

    private final BookRepository bookRepository;
    private final BookEmbeddingRepository embeddingRepo;
    private final AIService aiService;

    public BookRecommendationService(BookRepository bookRepository,
                                     BookEmbeddingRepository embeddingRepo,
                                     AIService aiService) {
        this.bookRepository = bookRepository;
        this.embeddingRepo = embeddingRepo;
        this.aiService = aiService;
    }

    /**
     * Gợi ý sách tương tự dựa trên embedding similarity
     * @param bookId ID của sách cần tìm sách tương tự
     * @param limit Số lượng sách gợi ý tối đa (mặc định 5)
     * @return Danh sách sách tương tự được sắp xếp theo độ tương đồng
     */
    @Transactional(readOnly = true)
    public List<BookResponse> recommendSimilarBooks(Long bookId, Integer limit) {
        if (bookId == null) {
            log.warn("⚠️ BookId null, trả về danh sách trống");
            return List.of();
        }

        int resultLimit = (limit != null && limit > 0) ? limit : DEFAULT_LIMIT;
        log.info("🎯 Bắt đầu gợi ý sách tương tự cho bookId: {}, limit: {}", bookId, resultLimit);

        try {
            Optional<Book> targetOpt = bookRepository.findById(bookId);
            if (targetOpt.isEmpty()) {
                log.warn("⚠️ Không tìm thấy sách với ID: {}", bookId);
                return List.of();
            }

            Book target = targetOpt.get();

            // 1. Lấy hoặc tạo embedding cho sách mục tiêu
            List<Double> targetVector = getOrCreateEmbedding(target);
            if (targetVector.isEmpty()) {
                log.warn("⚠️ Không thể tạo embedding cho sách: {}", target.getTitle());
                return List.of();
            }

            // 2. Load tất cả sách (trừ sách hiện tại)
            List<Book> allBooks = bookRepository.findAll();
            if (allBooks.isEmpty()) {
                log.info("📚 Không có sách nào trong database");
                return List.of();
            }

            // 3. Tính similarity với tất cả sách khác
            Map<Book, Double> similarityMap = new HashMap<>();
            int processedCount = 0;
            int skippedCount = 0;

            for (Book book : allBooks) {
                if (book.getId().equals(bookId)) {
                    skippedCount++;
                    continue;
                }

                List<Double> bookVector = getOrCreateEmbedding(book);
                if (bookVector.isEmpty()) {
                    skippedCount++;
                    continue;
                }

                double score = cosineSimilarity(targetVector, bookVector);
                if (score >= MIN_SIMILARITY_THRESHOLD) {
                    similarityMap.put(book, score);
                }
                processedCount++;
            }

            log.info("📊 Đã xử lý {} sách, bỏ qua {} sách", processedCount, skippedCount);

            // 4. Sắp xếp và lấy top kết quả
            List<Book> topBooks = similarityMap.entrySet().stream()
                    .sorted(Map.Entry.<Book, Double>comparingByValue().reversed())
                    .limit(resultLimit)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // 5. Map sang BookResponse và đảm bảo category được load
            List<BookResponse> results = topBooks.stream()
                    .map(book -> {
                        // Đảm bảo category được load trong transaction
                        if (book.getCategory() != null) {
                            book.getCategory().getName();
                        }
                        return BookMapper.toBookResponse(book);
                    })
                    .collect(Collectors.toList());

            log.info("✅ Đã gợi ý {} sách tương tự cho sách: {}", results.size(), target.getTitle());
            return results;

        } catch (Exception e) {
            log.error("❌ Lỗi khi thực hiện gợi ý sách: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Overload method với limit mặc định
     */
    @Transactional(readOnly = true)
    public List<BookResponse> recommendSimilarBooks(Long bookId) {
        return recommendSimilarBooks(bookId, DEFAULT_LIMIT);
    }

    /**
     * Lấy embedding từ database hoặc tạo mới nếu chưa có
     * Lưu ý: Chỉ đọc, không tạo mới trong read-only transaction
     */
    private List<Double> getOrCreateEmbedding(Book book) {
        Optional<BookEmbedding> embeddingOpt = embeddingRepo.findByBookId(book.getId());
        
        if (embeddingOpt.isPresent()) {
            BookEmbedding embedding = embeddingOpt.get();
            List<Double> vector = embedding.getEmbeddingVector();
            if (vector != null && !vector.isEmpty()) {
                return vector;
            }
        }

        // Nếu chưa có embedding, trả về empty (không tạo mới trong read-only transaction)
        log.debug("📝 Sách '{}' chưa có embedding", book.getTitle());
        return List.of();
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

        return dot / (denominator + 1e-10);
    }
}