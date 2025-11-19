package iuh.fit.haitebooks_backend.dtos.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PromotionRequest {
    private String name;
    private String code;
    private double discountPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    private int quantity;
    private Double minimumOrderAmount; // Điều kiện đơn hàng tối thiểu (VND), null = không có điều kiện
}
