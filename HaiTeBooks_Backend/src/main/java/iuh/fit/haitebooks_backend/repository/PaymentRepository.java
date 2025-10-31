package iuh.fit.haitebooks_backend.repository;

import iuh.fit.haitebooks_backend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderId(Long orderId);
    List<Payment> findByOrder_User_Id(Long userId);
}

