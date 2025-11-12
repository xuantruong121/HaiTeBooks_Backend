package iuh.fit.haitebooks_backend.repository;

import iuh.fit.haitebooks_backend.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserId(Long userId);
    List<Review> findByBookId(Long bookId);
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
}
