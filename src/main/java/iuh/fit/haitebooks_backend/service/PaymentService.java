package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.model.Payment;
import iuh.fit.haitebooks_backend.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment makePayment(Payment payment) {
        // implement payment processing logic or call gateway
        return paymentRepository.save(payment);
    }

    public List<Payment> findByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}
