package iuh.fit.haitebooks_backend.repository;

import iuh.fit.haitebooks_backend.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByBarcode(String barcode);
}
