package iuh.fit.haitebooks_backend.ai.service;

import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.Order;
import iuh.fit.haitebooks_backend.model.Order_Item;
import iuh.fit.haitebooks_backend.model.Review;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import iuh.fit.haitebooks_backend.repository.CartRepository;
import iuh.fit.haitebooks_backend.repository.FavoriteBookRepository;
import iuh.fit.haitebooks_backend.repository.OrderRepository;
import iuh.fit.haitebooks_backend.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Collaborative Filtering Service - Không cần train model
 * Sử dụng dữ liệu hành vi người dùng để gợi ý sách
 */
@Service
public class CollaborativeFilteringService {

    private static final Logger log = LoggerFactory.getLogger(CollaborativeFilteringService.class);

    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ReviewRepository reviewRepository;
    private final FavoriteBookRepository favoriteBookRepository;

    public CollaborativeFilteringService(BookRepository bookRepository,
                                        OrderRepository orderRepository,
                                        CartRepository cartRepository,
                                        ReviewRepository reviewRepository,
                                        FavoriteBookRepository favoriteBookRepository) {
        this.bookRepository = bookRepository;
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.reviewRepository = reviewRepository;
        this.favoriteBookRepository = favoriteBookRepository;
    }

    /**
     * Tính điểm số cho mỗi sách dựa trên hành vi người dùng
     * @param userId ID của user cần gợi ý
     * @return Map<BookId, Score> - Điểm số từ 0.0 đến 1.0
     */
    @Transactional(readOnly = true)
    public Map<Long, Double> calculateBookScores(Long userId) {
        Map<Long, Double> bookScores = new HashMap<>();

        // 1. Lấy danh sách sách user đã mua (quan trọng nhất - weight: 1.0)
        Set<Long> purchasedBookIds = getPurchasedBookIds(userId);
        for (Long bookId : purchasedBookIds) {
            bookScores.put(bookId, 1.0); // Đã mua = điểm cao nhất
        }

        // 2. "Users who bought X also bought Y" - Collaborative Filtering
        Map<Long, Double> alsoBoughtScores = calculateAlsoBoughtScores(purchasedBookIds, userId);
        mergeScores(bookScores, alsoBoughtScores, 0.7); // Weight: 0.7

        // 3. Sách trong giỏ hàng của user (quan tâm nhưng chưa mua - weight: 0.5)
        Set<Long> cartBookIds = getCartBookIds(userId);
        for (Long bookId : cartBookIds) {
            if (!purchasedBookIds.contains(bookId)) {
                bookScores.merge(bookId, 0.5, Math::max);
            }
        }

        // 4. Sách user đã đánh giá cao (rating >= 4 - weight: 0.6)
        Map<Long, Double> highRatedScores = getHighRatedBookScores(userId);
        mergeScores(bookScores, highRatedScores, 0.6);

        // 5. Sách user đã favorite (weight: 0.4)
        Set<Long> favoriteBookIds = getFavoriteBookIds(userId);
        for (Long bookId : favoriteBookIds) {
            if (!purchasedBookIds.contains(bookId)) {
                bookScores.merge(bookId, 0.4, Math::max);
            }
        }

        // 6. Sách trong cùng category với sách user đã mua (weight: 0.3)
        Map<Long, Double> categoryScores = calculateCategoryBasedScores(purchasedBookIds, userId);
        mergeScores(bookScores, categoryScores, 0.3);

        // 7. Sách cùng author với sách user đã mua (weight: 0.25)
        Map<Long, Double> authorScores = calculateAuthorBasedScores(purchasedBookIds, userId);
        mergeScores(bookScores, authorScores, 0.25);

        log.debug("📊 Đã tính điểm cho {} sách dựa trên collaborative filtering", bookScores.size());
        return bookScores;
    }

    /**
     * Tính điểm "Users who bought X also bought Y"
     * Đây là collaborative filtering cốt lõi
     */
    private Map<Long, Double> calculateAlsoBoughtScores(Set<Long> userPurchasedBookIds, Long excludeUserId) {
        Map<Long, Double> scores = new HashMap<>();
        
        if (userPurchasedBookIds.isEmpty()) {
            return scores;
        }

        // Tìm tất cả user đã mua ít nhất 1 sách giống user hiện tại
        List<Order> allOrders = orderRepository.findAll();
        Map<Long, Set<Long>> userPurchases = new HashMap<>(); // userId -> set of bookIds

        for (Order order : allOrders) {
            if (order.getUser() == null || order.getUser().getId().equals(excludeUserId)) {
                continue;
            }
            
            Long otherUserId = order.getUser().getId();
            Set<Long> otherUserBooks = userPurchases.computeIfAbsent(otherUserId, k -> new HashSet<>());
            
            if (order.getOrderItems() != null) {
                for (Order_Item item : order.getOrderItems()) {
                    if (item.getBook() != null) {
                        otherUserBooks.add(item.getBook().getId());
                    }
                }
            }
        }

        // Tính điểm: nếu user khác mua nhiều sách giống user hiện tại, 
        // thì các sách khác họ mua sẽ có điểm cao
        for (Map.Entry<Long, Set<Long>> entry : userPurchases.entrySet()) {
            Set<Long> otherUserBooks = entry.getValue();
            
            // Tính độ tương đồng (Jaccard similarity)
            Set<Long> intersection = new HashSet<>(userPurchasedBookIds);
            intersection.retainAll(otherUserBooks);
            
            if (intersection.isEmpty()) {
                continue; // Không có sách chung
            }

            Set<Long> union = new HashSet<>(userPurchasedBookIds);
            union.addAll(otherUserBooks);
            double similarity = (double) intersection.size() / union.size();

            // Các sách user khác mua nhưng user hiện tại chưa mua
            Set<Long> recommendedBooks = new HashSet<>(otherUserBooks);
            recommendedBooks.removeAll(userPurchasedBookIds);

            // Gán điểm dựa trên similarity
            for (Long bookId : recommendedBooks) {
                scores.merge(bookId, similarity, Math::max);
            }
        }

        log.debug("🎯 Tìm thấy {} sách từ 'Users who bought X also bought Y'", scores.size());
        return scores;
    }

    /**
     * Lấy danh sách sách user đã mua
     */
    private Set<Long> getPurchasedBookIds(Long userId) {
        List<Order> userOrders = orderRepository.findByUserId(userId);
        Set<Long> bookIds = new HashSet<>();
        
        for (Order order : userOrders) {
            if (order.getOrderItems() != null) {
                for (Order_Item item : order.getOrderItems()) {
                    if (item.getBook() != null) {
                        bookIds.add(item.getBook().getId());
                    }
                }
            }
        }
        
        return bookIds;
    }

    /**
     * Lấy danh sách sách trong giỏ hàng
     */
    private Set<Long> getCartBookIds(Long userId) {
        return cartRepository.findByUserId(userId).stream()
                .filter(cart -> cart.getBook() != null)
                .map(cart -> cart.getBook().getId())
                .collect(Collectors.toSet());
    }

    /**
     * Lấy điểm số cho sách user đã đánh giá cao (rating >= 4)
     */
    private Map<Long, Double> getHighRatedBookScores(Long userId) {
        List<Review> userReviews = reviewRepository.findByUserId(userId);
        Map<Long, Double> scores = new HashMap<>();
        
        for (Review review : userReviews) {
            if (review.getBook() != null && review.getRating() >= 4) {
                // Rating 4 = 0.5, Rating 5 = 0.6
                double score = 0.4 + (review.getRating() - 4) * 0.1;
                scores.put(review.getBook().getId(), score);
            }
        }
        
        return scores;
    }

    /**
     * Lấy danh sách sách user đã favorite
     */
    private Set<Long> getFavoriteBookIds(Long userId) {
        return favoriteBookRepository.findByUserId(userId).stream()
                .filter(fav -> fav.getBook() != null)
                .map(fav -> fav.getBook().getId())
                .collect(Collectors.toSet());
    }

    /**
     * Tính điểm dựa trên category - sách cùng category với sách đã mua
     */
    private Map<Long, Double> calculateCategoryBasedScores(Set<Long> purchasedBookIds, Long excludeUserId) {
        Map<Long, Double> scores = new HashMap<>();
        
        if (purchasedBookIds.isEmpty()) {
            return scores;
        }

        // Lấy categories của sách đã mua
        Set<Long> preferredCategories = new HashSet<>();
        List<Book> purchasedBooks = bookRepository.findAllById(purchasedBookIds);
        for (Book book : purchasedBooks) {
            if (book.getCategory() != null) {
                preferredCategories.add(book.getCategory().getId());
            }
        }

        // Tìm sách cùng category
        List<Book> allBooks = bookRepository.findAll();
        for (Book book : allBooks) {
            if (book.getCategory() != null && 
                preferredCategories.contains(book.getCategory().getId()) &&
                !purchasedBookIds.contains(book.getId())) {
                scores.put(book.getId(), 0.3);
            }
        }

        return scores;
    }

    /**
     * Tính điểm dựa trên author - sách cùng author với sách đã mua
     */
    private Map<Long, Double> calculateAuthorBasedScores(Set<Long> purchasedBookIds, Long excludeUserId) {
        Map<Long, Double> scores = new HashMap<>();
        
        if (purchasedBookIds.isEmpty()) {
            return scores;
        }

        // Lấy authors của sách đã mua
        Set<String> preferredAuthors = new HashSet<>();
        List<Book> purchasedBooks = bookRepository.findAllById(purchasedBookIds);
        for (Book book : purchasedBooks) {
            if (book.getAuthor() != null && !book.getAuthor().trim().isEmpty()) {
                preferredAuthors.add(book.getAuthor().trim().toLowerCase());
            }
        }

        // Tìm sách cùng author
        List<Book> allBooks = bookRepository.findAll();
        for (Book book : allBooks) {
            if (book.getAuthor() != null && 
                preferredAuthors.contains(book.getAuthor().trim().toLowerCase()) &&
                !purchasedBookIds.contains(book.getId())) {
                scores.put(book.getId(), 0.25);
            }
        }

        return scores;
    }

    /**
     * Merge scores vào map chính với weight
     */
    private void mergeScores(Map<Long, Double> mainScores, Map<Long, Double> newScores, double weight) {
        for (Map.Entry<Long, Double> entry : newScores.entrySet()) {
            Long bookId = entry.getKey();
            Double newScore = entry.getValue() * weight;
            mainScores.merge(bookId, newScore, Math::max);
        }
    }
}

