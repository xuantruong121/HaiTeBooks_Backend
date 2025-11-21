package iuh.fit.haitebooks_backend.repository;

import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.Cart;
import iuh.fit.haitebooks_backend.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    @EntityGraph(attributePaths = {"book", "user"})
    List<Cart> findByUserId(Long userId);
    
    @EntityGraph(attributePaths = {"book", "user"})
    Optional<Cart> findByUserAndBook(User user, Book book);
}
