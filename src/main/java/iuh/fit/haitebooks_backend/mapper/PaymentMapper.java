package iuh.fit.haitebooks_backend.mapper;

import iuh.fit.haitebooks_backend.dtos.request.PaymentRequest;
import iuh.fit.haitebooks_backend.dtos.response.PaymentResponse;
import iuh.fit.haitebooks_backend.model.Method;
import iuh.fit.haitebooks_backend.model.Order;
import iuh.fit.haitebooks_backend.model.Payment;
import iuh.fit.haitebooks_backend.model.Status_Payment;

import java.time.LocalDateTime;

public class PaymentMapper {

    public static Payment toEntity(PaymentRequest request, Order order) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(request.getAmount());
        payment.setMethod(Method.valueOf(request.getMethod().toUpperCase()));
        payment.setStatus(Status_Payment.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        return payment;
    }

    public static PaymentResponse toResponse(Payment payment) {
        if (payment == null) return null;

        return new PaymentResponse(
                payment.getId(),
                payment.getOrder() != null ? payment.getOrder().getId() : null,
                payment.getOrder() != null && payment.getOrder().getUser() != null
                        ? payment.getOrder().getUser().getId()
                        : null,
                payment.getMethod() != null ? payment.getMethod().name() : null, // ✅ Thêm method: CASH hoặc VNPAY
                payment.getAmount(),
                payment.getStatus() != null ? payment.getStatus().name() : null,
                payment.getPaymentDate()
        );
    }
}
