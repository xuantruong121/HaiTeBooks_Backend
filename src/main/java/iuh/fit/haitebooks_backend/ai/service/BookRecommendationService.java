package iuh.fit.haitebooks_backend.ai.service;

import iuh.fit.haitebooks_backend.dtos.response.BookResponse;
import iuh.fit.haitebooks_backend.mapper.BookMapper;
import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.BookEmbedding;
import iuh.fit.haitebooks_backend.model.Order;
import iuh.fit.haitebooks_backend.model.Order_Item;
import iuh.fit.haitebooks_backend.repository.BookEmbeddingRepository;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import iuh.fit.haitebooks_backend.repository.OrderRepository;
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

    // Trọng số cho hybrid recommendation
    private static final double CONTENT_BASED_WEIGHT = 0.4; // 40% cho content-based (embedding)
    private static final double COLLABORATIVE_WEIGHT = 0.6; // 60% cho collaborative filtering

    private final BookRepository bookRepository;
    private final BookEmbeddingRepository embeddingRepo;
    private final AIService aiService;
    private final CollaborativeFilteringService collaborativeFilteringService;
    private final OrderRepository orderRepository;

    public BookRecommendationService(BookRepository bookRepository,
                                     BookEmbeddingRepository embeddingRepo,
                                     AIService aiService,
                                     CollaborativeFilteringService collaborativeFilteringService,
                                     OrderRepository orderRepository) {
        this.bookRepository = bookRepository;
        this.embeddingRepo = embeddingRepo;
        this.aiService = aiService;
        this.collaborativeFilteringService = collaborativeFilteringService;
        this.orderRepository = orderRepository;
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
     * Hybrid Recommendation: Gợi ý sách cho user dựa trên Content-Based + Collaborative Filtering
     * Không cần train model - sử dụng dữ liệu hành vi hiện có
     * 
     * @param userId ID của user cần gợi ý
     * @param limit Số lượng sách gợi ý tối đa (mặc định 10)
     * @return Danh sách sách được gợi ý kết hợp từ nhiều phương pháp
     */
    @Transactional(readOnly = true)
    public List<BookResponse> recommendForUser(Long userId, Integer limit) {
        if (userId == null) {
            log.warn("⚠️ UserId null, trả về danh sách trống");
            return List.of();
        }

        int resultLimit = (limit != null && limit > 0) ? limit : 10;
        log.info("🎯 Bắt đầu hybrid recommendation cho userId: {}, limit: {}", userId, resultLimit);

        try {
            // 1. Collaborative Filtering: Tính điểm dựa trên hành vi người dùng
            Map<Long, Double> collaborativeScores = collaborativeFilteringService.calculateBookScores(userId);
            log.info("📊 Collaborative Filtering: {} sách có điểm", collaborativeScores.size());

            // 2. Content-Based: Tính điểm dựa trên embedding của sách user đã mua/thích
            Map<Long, Double> contentBasedScores = calculateContentBasedScores(userId);
            log.info("📊 Content-Based: {} sách có điểm", contentBasedScores.size());

            // 3. Kết hợp 2 phương pháp (Hybrid)
            Map<Long, Double> hybridScores = new HashMap<>();
            
            // Merge collaborative scores
            for (Map.Entry<Long, Double> entry : collaborativeScores.entrySet()) {
                double score = entry.getValue() * COLLABORATIVE_WEIGHT;
                hybridScores.put(entry.getKey(), score);
            }
            
            // Merge content-based scores
            for (Map.Entry<Long, Double> entry : contentBasedScores.entrySet()) {
                hybridScores.merge(entry.getKey(), 
                    entry.getValue() * CONTENT_BASED_WEIGHT, 
                    (oldVal, newVal) -> oldVal + newVal); // Cộng dồn nếu có cả 2
            }

            // 4. Sắp xếp và lấy top kết quả
            List<Book> topBooks = hybridScores.entrySet().stream()
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .limit(resultLimit)
                    .map(entry -> bookRepository.findById(entry.getKey()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            // 5. Map sang BookResponse
            List<BookResponse> results = topBooks.stream()
                    .map(book -> {
                        if (book.getCategory() != null) {
                            book.getCategory().getName();
                        }
                        return BookMapper.toBookResponse(book);
                    })
                    .collect(Collectors.toList());

            log.info("✅ Đã gợi ý {} sách cho user {}", results.size(), userId);
            return results;

        } catch (Exception e) {
            log.error("❌ Lỗi khi thực hiện hybrid recommendation: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Tính điểm Content-Based dựa trên embedding của sách user đã mua/thích
     */
    private Map<Long, Double> calculateContentBasedScores(Long userId) {
        Map<Long, Double> scores = new HashMap<>();
        
        // Lấy danh sách sách user đã mua từ orders
        Set<Long> userBookIds = new HashSet<>();
        List<Order> userOrders = orderRepository.findByUserId(userId);
        for (Order order : userOrders) {
            if (order.getOrderItems() != null) {
                for (Order_Item item : order.getOrderItems()) {
                    if (item.getBook() != null) {
                        userBookIds.add(item.getBook().getId());
                    }
                }
            }
        }

        // Nếu user chưa mua sách nào, trả về empty (để collaborative filtering làm việc)
        if (userBookIds.isEmpty()) {
            log.debug("📝 User {} chưa mua sách nào, bỏ qua content-based", userId);
            return scores;
        }

        // Tính average embedding của sách user đã mua
        List<Double> userProfileVector = calculateUserProfileVector(userBookIds);
        if (userProfileVector.isEmpty()) {
            return scores;
        }

        // So sánh với tất cả sách khác
        List<Book> allBooks = bookRepository.findAll();
        for (Book book : allBooks) {
            if (userBookIds.contains(book.getId())) {
                continue; // Bỏ qua sách đã có
            }

            List<Double> bookVector = getOrCreateEmbedding(book);
            if (bookVector.isEmpty()) {
                continue;
            }

            double similarity = cosineSimilarity(userProfileVector, bookVector);
            if (similarity >= MIN_SIMILARITY_THRESHOLD) {
                scores.put(book.getId(), similarity);
            }
        }

        return scores;
    }

    /**
     * Tính average embedding vector từ danh sách sách (user profile)
     */
    private List<Double> calculateUserProfileVector(Set<Long> bookIds) {
        List<List<Double>> vectors = new ArrayList<>();
        
        for (Long bookId : bookIds) {
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            if (bookOpt.isPresent()) {
                List<Double> vector = getOrCreateEmbedding(bookOpt.get());
                if (!vector.isEmpty()) {
                    vectors.add(vector);
                }
            }
        }

        if (vectors.isEmpty()) {
            return List.of();
        }

        // Tính average vector
        int dimension = vectors.get(0).size();
        List<Double> averageVector = new ArrayList<>(Collections.nCopies(dimension, 0.0));
        
        for (List<Double> vector : vectors) {
            for (int i = 0; i < dimension; i++) {
                averageVector.set(i, averageVector.get(i) + vector.get(i));
            }
        }
        
        int count = vectors.size();
        for (int i = 0; i < dimension; i++) {
            averageVector.set(i, averageVector.get(i) / count);
        }

        return averageVector;
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