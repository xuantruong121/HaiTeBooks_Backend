package iuh.fit.haitebooks_backend.repository;

import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.FavoriteBook;
import iuh.fit.haitebooks_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteBookRepository extends JpaRepository<FavoriteBook, Long> {
    List<FavoriteBook> findByUserId(Long userId);
    Optional<FavoriteBook> findByUserAndBook(User user, Book book);
    boolean existsByUserAndBook(User user, Book book);
    void deleteByUserAndBook(User user, Book book);
}
