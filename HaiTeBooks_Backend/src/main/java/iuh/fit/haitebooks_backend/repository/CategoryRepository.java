package iuh.fit.haitebooks_backend.repository;

import iuh.fit.haitebooks_backend.model.BookCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<BookCategory, Long> {
}
