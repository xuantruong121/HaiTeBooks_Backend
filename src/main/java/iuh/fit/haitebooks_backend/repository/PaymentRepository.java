package iuh.fit.haitebooks_backend.repository;

import iuh.fit.haitebooks_backend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderId(Long orderId);
    List<Payment> findByOrder_User_Id(Long userId);
    Optional<Payment> findByVnpTxnRef(String vnpTxnRef); // VNPay txn ref
}

