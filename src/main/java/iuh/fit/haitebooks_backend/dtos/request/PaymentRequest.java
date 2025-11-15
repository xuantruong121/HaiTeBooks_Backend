package iuh.fit.haitebooks_backend.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @Min(value = 1, message = "Amount must be greater than 0")
    private double amount;

    @NotNull(message = "Payment method is required")
    private String method; // e.g., VNPay

    @NotNull(message = "Order info is required")
    private String orderInfo;
}
