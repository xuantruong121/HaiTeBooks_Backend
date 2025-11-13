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
    private boolean active;
    private Long createdBy;
    private Long approvedBy;
}
