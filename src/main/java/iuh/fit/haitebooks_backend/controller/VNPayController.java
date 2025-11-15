package iuh.fit.haitebooks_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.haitebooks_backend.dtos.request.PaymentRequest;
import iuh.fit.haitebooks_backend.model.Status_Payment;
import iuh.fit.haitebooks_backend.service.PaymentService;
import iuh.fit.haitebooks_backend.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
public class VNPayController {

    private final VNPayService vnPayService;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public VNPayController(VNPayService vnPayService, PaymentService paymentService, ObjectMapper objectMapper) {
        this.vnPayService = vnPayService;
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

        // ✅ Helper method: Chuyển Map thành JSON string (chỉ lưu các thông tin quan trọng)
    private String mapToJsonString(Map<String, String> params) {
        try {
            // Chỉ lưu các thông tin quan trọng từ VNPay response
            Map<String, String> importantParams = new HashMap<>();
            importantParams.put("vnp_TxnRef", params.get("vnp_TxnRef"));
            importantParams.put("vnp_ResponseCode", params.get("vnp_ResponseCode"));
            importantParams.put("vnp_TransactionStatus", params.get("vnp_TransactionStatus"));
            importantParams.put("vnp_TransactionNo", params.get("vnp_TransactionNo"));
            importantParams.put("vnp_BankCode", params.get("vnp_BankCode"));
            importantParams.put("vnp_PayDate", params.get("vnp_PayDate"));
            importantParams.put("vnp_Amount", params.get("vnp_Amount"));
            importantParams.put("vnp_OrderInfo", params.get("vnp_OrderInfo"));
            
            String json = objectMapper.writeValueAsString(importantParams);
            
            // ✅ Giới hạn tối đa 50KB để đảm bảo không vượt quá TEXT (65KB)
            // MEDIUMTEXT có thể lưu 16MB nhưng giới hạn ở đây để an toàn
            int maxLength = 50000; // 50KB
            if (json.length() > maxLength) {
                return json.substring(0, maxLength) + "...[truncated]";
            }
            return json;
        } catch (Exception e) {
            // Fallback: chỉ lưu các thông tin tối thiểu nếu JSON serialize fail
            return String.format("{\"vnp_TxnRef\":\"%s\",\"vnp_ResponseCode\":\"%s\",\"vnp_TransactionStatus\":\"%s\",\"error\":\"serialization_failed\"}",
                    params.get("vnp_TxnRef"),
                    params.get("vnp_ResponseCode"),
                    params.get("vnp_TransactionStatus"));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequest request, HttpServletRequest httpReq) {
        try {
            // create unique txnRef: orderId-timestamp-random
            String txnRef = request.getOrderId() + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0,6);

            // create a payment record with PENDING
            paymentService.createPaymentRecordPending(request, txnRef);

            String url = vnPayService.createPaymentUrl(request, httpReq, txnRef);
            return ResponseEntity.ok(Map.of("paymentUrl", url, "txnRef", txnRef));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Encoding error"));
        }
    }

    // IPN: server-to-server notification from VNPay
    @PostMapping("/vnpay/ipn")
    public ResponseEntity<String> handleVNPayIPN(HttpServletRequest req) {
        Map<String, String> params = vnPayService.extractParams(req);

        // verify signature
        boolean ok = vnPayService.validateSignature(params);
        if (!ok) {
            return ResponseEntity.ok("responseCode=97"); // signature invalid
        }

        String txnRef = params.get("vnp_TxnRef");
        String amountStr = params.get("vnp_Amount"); // amount in cents
        String rspCode = params.get("vnp_ResponseCode"); // 00 success
        String txnStatus = params.get("vnp_TransactionStatus"); // sometimes used

        // find payment by txnRef
        var maybePayment = paymentService.findByVnpTxnRef(txnRef);
        if (maybePayment.isEmpty()) {
            return ResponseEntity.ok("responseCode=01"); // order not found
        }

        long amountFromVNPay = 0;
        try {
            amountFromVNPay = Long.parseLong(amountStr);
        } catch (Exception e) {
            // ignore parse error
        }
        long expected = (long)(maybePayment.get().getAmount() * 100);

        if (amountFromVNPay != expected) {
            // amount mismatch
            paymentService.markPaymentFailed(txnRef, mapToJsonString(params));
            return ResponseEntity.ok("responseCode=04"); // amount mismatch
        }

        // VNPay successful response code is "00"
        if ("00".equals(rspCode) || "00".equals(txnStatus)) {
            paymentService.markPaymentSuccess(txnRef, mapToJsonString(params));
            return ResponseEntity.ok("responseCode=00"); // success
        } else {
            paymentService.markPaymentFailed(txnRef, mapToJsonString(params));
            return ResponseEntity.ok("responseCode=99"); // payment failed
        }
    }

        // returnUrl: user will be redirected here after payment (GET)
    @GetMapping("/vnpay/return")
    public ResponseEntity<String> handleReturnFromVNPay(HttpServletRequest req) {
        Map<String, String> params = vnPayService.extractParams(req);
        
        // verify signature
        boolean valid = vnPayService.validateSignature(params);

        String txnRef = params.get("vnp_TxnRef");
        String rspCode = params.get("vnp_ResponseCode");
        String txnStatus = params.get("vnp_TransactionStatus");

        // ✅ Kiểm tra cả ResponseCode và TransactionStatus
        boolean isSuccess = "00".equals(rspCode) || "00".equals(txnStatus);
        
        String body;
        if (valid && isSuccess) {
            // ✅ Cập nhật payment status nếu chưa được cập nhật
            paymentService.findByVnpTxnRef(txnRef).ifPresent(payment -> {
                if (payment.getStatus() != Status_Payment.SUCCESS) {
                    paymentService.markPaymentSuccess(txnRef, mapToJsonString(params));
                }
            });
            body = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Thanh toán thành công</title></head>" +
                    "<body><h3>Thanh toán thành công. Mã giao dịch: " + txnRef + "</h3></body></html>";
        } else {
            body = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Thanh toán thất bại</title></head>" +
                    "<body><h3>Giao dịch thất bại hoặc không hợp lệ.</h3></body></html>";
        }
        // ✅ Thêm charset UTF-8 vào Content-Type
        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(body);
    }
}
