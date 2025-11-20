package iuh.fit.haitebooks_backend.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    private Double total;
    private String status;
    private List<OrderItemRequest> orderItems;
    private String address;
    private String note;
    private String promotionCode;
    
    // ✅ Thêm field paymentMethod để nhận phương thức thanh toán từ frontend
    private String paymentMethod; // "CASH" hoặc "VNPAY"
}
