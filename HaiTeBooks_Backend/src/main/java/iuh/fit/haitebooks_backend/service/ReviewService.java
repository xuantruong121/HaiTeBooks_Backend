package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.model.Review;
import iuh.fit.haitebooks_backend.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    public List<Review> findByBook(Long bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    public List<Review> findByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }
}
