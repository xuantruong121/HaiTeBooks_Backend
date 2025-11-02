package iuh.fit.haitebooks_backend.repository;

import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.Cart;
import iuh.fit.haitebooks_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserId(Long userId);
    Optional<Cart> findByUserAndBook(User user, Book book);
}
