package iuh.fit.haitebooks_backend.dtos.request;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long orderId;
    private Long userId;
    private double amount;
    private String method;
}
