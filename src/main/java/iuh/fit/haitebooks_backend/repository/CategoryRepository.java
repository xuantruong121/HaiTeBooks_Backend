package iuh.fit.haitebooks_backend.repository;

import iuh.fit.haitebooks_backend.model.BookCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<BookCategory, Long> {
    Optional<BookCategory> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
