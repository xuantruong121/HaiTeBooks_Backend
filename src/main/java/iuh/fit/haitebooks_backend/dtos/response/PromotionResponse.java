package iuh.fit.haitebooks_backend.dtos.response;

import lombok.Data;

@Data
public class PromotionResponse {
    private Long id;
    private String name;
    private String code;
    private double discountPercent;
    private String startDate;
    private String endDate;
    private int quantity;
    private Double minimumOrderAmount; // Điều kiện đơn hàng tối thiểu (VND), null = không có điều kiện
    private Double maxDiscountAmount; // Giảm tối đa bao nhiêu tiền (VND), null = không giới hạn
    private boolean active;
    private Long createdBy;
    private Long approvedBy;
}
