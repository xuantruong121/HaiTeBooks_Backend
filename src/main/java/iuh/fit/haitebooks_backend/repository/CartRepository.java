package iuh.fit.haitebooks_backend.repository;

import iuh.fit.haitebooks_backend.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserId(Long userId);
}
