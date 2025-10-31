package iuh.fit.haitebooks_backend.repository;

import iuh.fit.haitebooks_backend.model.BookEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookEmbeddingRepository extends JpaRepository<BookEmbedding, Long> {
    Optional<BookEmbedding> findByBookId(Long bookId);
}
