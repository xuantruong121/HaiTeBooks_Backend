package iuh.fit.haitebooks_backend.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private double total;
    private String status;
    private LocalDateTime orderDate;
    private String address;
    private String note;
    private List<OrderItemResponse> items;
    // ✅ Thêm field này
    private PromotionResponse appliedPromotion;
    // ✅ Thêm field paymentMethod để trả về phương thức thanh toán
    private String paymentMethod; // "CASH" hoặc "VNPAY"
}
