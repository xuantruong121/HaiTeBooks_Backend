package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.request.PaymentRequest;
import iuh.fit.haitebooks_backend.dtos.response.PaymentResponse;
import iuh.fit.haitebooks_backend.mapper.PaymentMapper;
import iuh.fit.haitebooks_backend.model.Payment;
import iuh.fit.haitebooks_backend.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ✅ Tạo thanh toán
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        Payment payment = paymentService.createPayment(request);
        return ResponseEntity.ok(PaymentMapper.toResponse(payment));
    }

    // ✅ Lấy thanh toán theo order
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrder(@PathVariable Long orderId) {
        List<PaymentResponse> responses = paymentService.findByOrder(orderId)
                .stream()
                .map(PaymentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // ✅ Lấy thanh toán theo user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUser(@PathVariable Long userId) {
        List<PaymentResponse> responses = paymentService.findByUser(userId)
                .stream()
                .map(PaymentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
