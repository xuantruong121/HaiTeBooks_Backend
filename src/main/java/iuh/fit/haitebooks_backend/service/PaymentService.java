package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.PaymentRequest;
import iuh.fit.haitebooks_backend.mapper.PaymentMapper;
import iuh.fit.haitebooks_backend.model.*;
import iuh.fit.haitebooks_backend.repository.OrderRepository;
import iuh.fit.haitebooks_backend.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    // ✅ Tạo thanh toán mới
    @Transactional
    public Payment createPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id " + request.getOrderId()));

        // Kiểm tra trùng thanh toán
        if (order.getPayment() != null) {
            throw new RuntimeException("This order has already been paid");
        }

        // Tạo Payment entity
        Payment payment = PaymentMapper.toEntity(request, order);

        // Gán Payment vào Order (liên kết hai chiều)
        order.setPayment(payment);
        order.setStatus(Status_Order.COMPLETED);
        payment.setStatus(Status_Payment.SUCCESS);

        return paymentRepository.save(payment);
    }

    // ✅ Tìm thanh toán theo order
    public List<Payment> findByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    // ✅ Tìm thanh toán theo user
    public List<Payment> findByUser(Long userId) {
        return paymentRepository.findByOrder_User_Id(userId);
    }
}
