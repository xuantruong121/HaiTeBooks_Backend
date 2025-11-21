package iuh.fit.haitebooks_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.haitebooks_backend.dtos.request.PaymentRequest;
import iuh.fit.haitebooks_backend.exception.ConflictException;
import iuh.fit.haitebooks_backend.exception.NotFoundException;
import iuh.fit.haitebooks_backend.mapper.PaymentMapper;
import iuh.fit.haitebooks_backend.model.*;
import iuh.fit.haitebooks_backend.repository.OrderRepository;
import iuh.fit.haitebooks_backend.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
    }

    // ✅ Tạo thanh toán mới
    @Transactional
    public Payment createPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found with id " + request.getOrderId()));

        // Kiểm tra trùng thanh toán
        if (order.getPayment() != null) {
            throw new ConflictException("This order has already been paid");
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
    @Transactional(readOnly = true)
    public List<Payment> findByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    // ✅ Tìm thanh toán theo user
    @Transactional(readOnly = true)
    public List<Payment> findByUser(Long userId) {
        return paymentRepository.findByOrder_User_Id(userId);
    }

    @Transactional
    public Payment createPaymentRecordPending(PaymentRequest request, String txnRef) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // ✅ Kiểm tra payment đã tồn tại từ repository (chính xác hơn)
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(request.getOrderId())
                .stream()
                .findFirst();

        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();

            // Nếu payment đã SUCCESS, không cho tạo lại
            if (payment.getStatus() == Status_Payment.SUCCESS) {
                throw new ConflictException("This order has already been paid successfully");
            }

            // Nếu payment FAILED hoặc PENDING, cập nhật payment cũ
            if (payment.getStatus() == Status_Payment.FAILED ||
                    payment.getStatus() == Status_Payment.PENDING) {
                // Cập nhật payment cũ thay vì tạo mới
                payment.setStatus(Status_Payment.PENDING);
                payment.setVnpTxnRef(txnRef);
                payment.setAmount(request.getAmount());
                payment.setMethod(Method.valueOf(request.getMethod().toUpperCase()));
                payment.setPaymentDate(LocalDateTime.now());
                payment.setRawResponse(null); // Xóa rawResponse cũ

                // Đảm bảo order status là PENDING
                order.setStatus(Status_Order.PENDING);
                orderRepository.save(order);

                // Chỉ save payment, không save order nữa (tránh cascade duplicate)
                return paymentRepository.save(payment);
            }
        }

        // Chỉ tạo payment mới nếu chưa có payment nào
        Payment p = new Payment();
        p.setOrder(order);
        p.setAmount(request.getAmount());
        p.setMethod(Method.valueOf(request.getMethod().toUpperCase()));
        p.setStatus(Status_Payment.PENDING);
        p.setVnpTxnRef(txnRef);
        p.setPaymentDate(LocalDateTime.now());

        // Set payment vào order để đồng bộ quan hệ
        order.setPayment(p);
        order.setStatus(Status_Order.PENDING);

        // ✅ QUAN TRỌNG: Save payment TRƯỚC, sau đó mới save order
        // Hoặc không save order nữa vì cascade sẽ tự động save payment
        Payment savedPayment = paymentRepository.save(p);
        orderRepository.save(order);

        return savedPayment;
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findByVnpTxnRef(String txnRef) {
        return paymentRepository.findByVnpTxnRef(txnRef);
    }

    /**
     * ✅ Parse JSON từ rawResponse và set vào các trường tương ứng
     */
    private void parseAndSetVNPayFields(Payment payment, String rawResponseJson) {
        try {
            // Parse JSON string thành Map
            Map<String, String> params = objectMapper.readValue(rawResponseJson,
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));

            // Set các trường tương ứng
            if (params.get("vnp_TransactionNo") != null) {
                payment.setVnpTransactionNo(params.get("vnp_TransactionNo"));
            }
            if (params.get("vnp_PayDate") != null) {
                payment.setVnpPayDate(params.get("vnp_PayDate"));
            }
            if (params.get("vnp_ResponseCode") != null) {
                payment.setVnpResponseCode(params.get("vnp_ResponseCode"));
            }
            if (params.get("vnp_BankCode") != null) {
                payment.setVnpBankCode(params.get("vnp_BankCode"));
            }
            // vnp_TxnRef đã được set trước đó, không cần set lại
        } catch (Exception e) {
            // Nếu parse fail, chỉ log warning, không throw exception
            // Các trường sẽ giữ giá trị cũ hoặc null
            log.warn("Warning: Failed to parse VNPay response JSON: {}", e.getMessage());
        }
    }

    @Transactional
    public void markPaymentSuccess(String txnRef, String rawResponse) {
        Payment payment = paymentRepository.findByVnpTxnRef(txnRef)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + txnRef));

        payment.setStatus(Status_Payment.SUCCESS);
        payment.setPaymentDate(LocalDateTime.now());

        // ✅ Parse JSON và set vào các trường tương ứng
        parseAndSetVNPayFields(payment, rawResponse);

        // ✅ Lưu raw_response (optional - có thể bỏ nếu không cần audit)
        payment.setRawResponse(rawResponse);

        paymentRepository.save(payment);

        // ✅ SỬA: KHÔNG thay đổi order status
        // Order vẫn giữ nguyên status PENDING sau khi thanh toán thành công
        // Order chỉ chuyển sang COMPLETED khi đã được xác nhận và giao hàng
        Order order = payment.getOrder();
        // ❌ XÓA: Không set order status = COMPLETED
        // order.setStatus(Status_Order.COMPLETED);
        // ✅ Giữ nguyên status hiện tại (PENDING)
        // Không cần save order vì không có thay đổi
        // orderRepository.save(order);
    }

    @Transactional
    public void markPaymentFailed(String txnRef, String rawResponse) {
        paymentRepository.findByVnpTxnRef(txnRef).ifPresent(p -> {
            p.setStatus(Status_Payment.FAILED);

            // ✅ Parse JSON và set vào các trường tương ứng (nếu có)
            if (rawResponse != null && !rawResponse.isEmpty()) {
                parseAndSetVNPayFields(p, rawResponse);
            }

            // ✅ Lưu raw_response (optional - có thể bỏ nếu không cần audit)
            p.setRawResponse(rawResponse);

            paymentRepository.save(p);
        });
    }
}