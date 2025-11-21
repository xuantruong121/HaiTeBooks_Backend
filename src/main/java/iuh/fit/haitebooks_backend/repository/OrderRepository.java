package iuh.fit.haitebooks_backend.repository;

import iuh.fit.haitebooks_backend.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long> {
    @EntityGraph(attributePaths = {"user", "orderItems", "orderItems.book", "appliedPromotion"})
    List<Order> findByUserId(Long userId);
    
    @EntityGraph(attributePaths = {"user", "orderItems", "orderItems.book", "appliedPromotion"})
    @Override
    List<Order> findAll();
    
    @EntityGraph(attributePaths = {"user", "orderItems", "orderItems.book", "appliedPromotion"})
    @Override
    Optional<Order> findById(Long id);
}
