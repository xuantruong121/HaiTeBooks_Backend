package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.request.PaymentRequest;
import iuh.fit.haitebooks_backend.dtos.response.PaymentResponse;
import iuh.fit.haitebooks_backend.model.Payment;
import iuh.fit.haitebooks_backend.repository.PaymentRepository;
import iuh.fit.haitebooks_backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentService paymentService, PaymentRepository paymentRepository) {
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.makePayment(payment));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrder(@PathVariable Long orderId) {
        List<PaymentResponse> responses = paymentService.findByOrder(orderId)
                .stream()
                .map(p -> new PaymentResponse(p.getId(), p.getOrder().getId(), p.getOrder().getUser().getId(),
                        p.getAmount(), p.getStatus().name(), p.getPaymentDate()))
                .toList();
        return ResponseEntity.ok(responses);
    }
}
