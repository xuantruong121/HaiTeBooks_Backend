package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.request.ReviewRequest;
import iuh.fit.haitebooks_backend.dtos.response.ReviewResponse;
import iuh.fit.haitebooks_backend.mapper.ReviewMapper;
import iuh.fit.haitebooks_backend.model.Review;
import iuh.fit.haitebooks_backend.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // ✅ Tạo review
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        Review review = reviewService.createReview(request);
        return ResponseEntity.ok(ReviewMapper.toResponse(review));
    }

    // ✅ Lấy tất cả review
    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getAllReviews() {
        List<ReviewResponse> responses = reviewService.getAll()
                .stream()
                .map(ReviewMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // ✅ Lấy review theo sách
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ReviewResponse>> getByBook(@PathVariable Long bookId) {
        List<ReviewResponse> responses = reviewService.findByBook(bookId)
                .stream()
                .map(ReviewMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // ✅ Lấy review theo user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getByUser(@PathVariable Long userId) {
        List<ReviewResponse> responses = reviewService.findByUser(userId)
                .stream()
                .map(ReviewMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
