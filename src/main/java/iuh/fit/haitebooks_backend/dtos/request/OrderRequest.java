package iuh.fit.haitebooks_backend.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    private Double total;
    private String status;
    
    @NotNull(message = "Order items are required")
    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemRequest> orderItems;
    private String address;
    private String note;
    private String promotionCode;
    
    // ✅ Thêm field paymentMethod để nhận phương thức thanh toán từ frontend
    private String paymentMethod; // "CASH" hoặc "VNPAY"
}
