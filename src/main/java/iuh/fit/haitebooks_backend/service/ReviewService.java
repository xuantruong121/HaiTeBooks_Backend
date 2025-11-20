package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.ReviewRequest;
import iuh.fit.haitebooks_backend.dtos.response.ReviewResponse;
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
    public Review createReview(ReviewRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id " + request.getUserId()));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with id " + request.getBookId()));

        // Kiểm tra trùng review
        boolean alreadyReviewed = reviewRepository.existsByUserIdAndBookId(user.getId(), book.getId());
        if (alreadyReviewed) {
            throw new RuntimeException("User has already reviewed this book");
        }

        Review review = ReviewMapper.toEntity(request, user, book);
        return reviewRepository.save(review);
    }

    // ✅ Lấy tất cả review
    @Transactional(readOnly = true)
    public List<Review> getAll() {
        return reviewRepository.findAll();
    }

    // ✅ Lấy review theo sách
    @Transactional(readOnly = true)
    public List<Review> findByBook(Long bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    // ✅ Lấy review theo user
    @Transactional(readOnly = true)
    public List<Review> findByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public ReviewResponse toResponse(Review review) {
        // Đảm bảo lazy relationships được load trong transaction
        if (review.getUser() != null) {
            review.getUser().getId();
        }
        if (review.getBook() != null) {
            review.getBook().getId();
        }
        return ReviewMapper.toResponse(review);
    }
}
