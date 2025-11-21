package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.ReviewRequest;
import iuh.fit.haitebooks_backend.dtos.response.ReviewResponse;
import iuh.fit.haitebooks_backend.exception.ConflictException;
import iuh.fit.haitebooks_backend.exception.NotFoundException;
import iuh.fit.haitebooks_backend.mapper.ReviewMapper;
import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.Review;
import iuh.fit.haitebooks_backend.model.User;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import iuh.fit.haitebooks_backend.repository.ReviewRepository;
import iuh.fit.haitebooks_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         BookRepository bookRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    // ✅ Tạo mới review
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with id " + request.getUserId()));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new NotFoundException("Book not found with id " + request.getBookId()));

        // Kiểm tra trùng review
        boolean alreadyReviewed = reviewRepository.existsByUserIdAndBookId(user.getId(), book.getId());
        if (alreadyReviewed) {
            throw new ConflictException("User has already reviewed this book");
        }

        Review review = ReviewMapper.toEntity(request, user, book);
        review = reviewRepository.save(review);
        
        // Đảm bảo lazy relationships được load trong transaction
        loadLazyRelationships(review);
        return ReviewMapper.toResponse(review);
    }

    // ✅ Lấy tất cả review
    @Transactional(readOnly = true)
    public List<ReviewResponse> getAll() {
        List<Review> reviews = reviewRepository.findAll();
        // Map trong transaction để đảm bảo lazy relationships được load
        return reviews.stream()
                .map(review -> {
                    loadLazyRelationships(review);
                    return ReviewMapper.toResponse(review);
                })
                .toList();
    }

    // ✅ Lấy review theo sách
    @Transactional(readOnly = true)
    public List<ReviewResponse> findByBook(Long bookId) {
        List<Review> reviews = reviewRepository.findByBookId(bookId);
        // Map trong transaction để đảm bảo lazy relationships được load
        return reviews.stream()
                .map(review -> {
                    loadLazyRelationships(review);
                    return ReviewMapper.toResponse(review);
                })
                .toList();
    }

    // ✅ Lấy review theo user
    @Transactional(readOnly = true)
    public List<ReviewResponse> findByUser(Long userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        // Map trong transaction để đảm bảo lazy relationships được load
        return reviews.stream()
                .map(review -> {
                    loadLazyRelationships(review);
                    return ReviewMapper.toResponse(review);
                })
                .toList();
    }

    // ✅ Cập nhật review
    @Transactional
    public ReviewResponse updateReview(Long id, ReviewRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found with id " + id));

        // Chỉ cập nhật rating và comment, không thay đổi user và book
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        
        review = reviewRepository.save(review);
        
        // Đảm bảo lazy relationships được load trong transaction
        loadLazyRelationships(review);
        return ReviewMapper.toResponse(review);
    }

    // ✅ Xóa review
    @Transactional
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new NotFoundException("Review not found with id " + id);
        }
        reviewRepository.deleteById(id);
    }

    /**
     * Đảm bảo lazy relationships được load trong transaction
     */
    private void loadLazyRelationships(Review review) {
        if (review.getUser() != null) {
            review.getUser().getId();
            review.getUser().getFullName(); // Trigger load để lấy userName
        }
        if (review.getBook() != null) {
            review.getBook().getId();
            review.getBook().getTitle(); // Trigger load để lấy bookTitle
        }
    }
}
