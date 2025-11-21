package iuh.fit.haitebooks_backend.repository;

import iuh.fit.haitebooks_backend.model.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @EntityGraph(attributePaths = {"book", "user"})
    @Override
    List<Review> findAll();
    
    // ✅ Tối ưu: Eager fetch book và user khi tìm theo ID
    @EntityGraph(attributePaths = {"book", "user"})
    @Override
    java.util.Optional<Review> findById(Long id);
    
    @EntityGraph(attributePaths = {"book", "user"})
    List<Review> findByUserId(Long userId);
    
    @EntityGraph(attributePaths = {"book", "user"})
    List<Review> findByBookId(Long bookId);
    
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
}
