package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.response.ReviewResponse;
import iuh.fit.haitebooks_backend.model.Review;
import iuh.fit.haitebooks_backend.repository.ReviewRepository;
import iuh.fit.haitebooks_backend.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;

    public ReviewController(ReviewService reviewService, ReviewRepository reviewRepository) {
        this.reviewService = reviewService;
        this.reviewRepository = reviewRepository;
    }

    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        return ResponseEntity.ok(reviewService.save(review));
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ReviewResponse>> getByBook(@PathVariable Long bookId) {
        List<ReviewResponse> responses = reviewService.findByBook(bookId)
                .stream()
                .map(r -> new ReviewResponse(r.getId(), r.getUser().getId(), r.getBook().getId(),
                        r.getRating(), r.getComment(), r.getCreated_at()))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getByUser(@PathVariable Long userId) {
        List<ReviewResponse> responses = reviewService.findByUser(userId)
                .stream()
                .map(r -> new ReviewResponse(r.getId(), r.getUser().getId(), r.getBook().getId(),
                        r.getRating(), r.getComment(), r.getCreated_at()))
                .toList();
        return ResponseEntity.ok(responses);
    }
}
