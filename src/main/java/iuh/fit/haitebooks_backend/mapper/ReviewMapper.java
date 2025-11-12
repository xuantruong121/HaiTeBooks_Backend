package iuh.fit.haitebooks_backend.mapper;

import iuh.fit.haitebooks_backend.dtos.request.ReviewRequest;
import iuh.fit.haitebooks_backend.dtos.response.ReviewResponse;
import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.Review;
import iuh.fit.haitebooks_backend.model.User;

import java.time.LocalDateTime;

public class ReviewMapper {

    public static Review toEntity(ReviewRequest request, User user, Book book) {
        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCreated_at(LocalDateTime.now());
        return review;
    }

    public static ReviewResponse toResponse(Review review) {
        if (review == null) return null;

        return new ReviewResponse(
                review.getId(),
                review.getUser() != null ? review.getUser().getId() : null,
                review.getBook() != null ? review.getBook().getId() : null,
                review.getRating(),
                review.getComment(),
                review.getCreated_at()
        );
    }
}
