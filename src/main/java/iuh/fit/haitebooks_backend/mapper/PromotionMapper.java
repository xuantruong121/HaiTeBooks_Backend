package iuh.fit.haitebooks_backend.mapper;

import iuh.fit.haitebooks_backend.dtos.response.PromotionResponse;
import iuh.fit.haitebooks_backend.model.Promotion;

public class PromotionMapper {

    public static PromotionResponse toResponse(Promotion p) {
        PromotionResponse res = new PromotionResponse();
        res.setId(p.getId());
        res.setName(p.getName());
        res.setCode(p.getCode());
        res.setDiscountPercent(p.getDiscountPercent());
        res.setStartDate(p.getStartDate().toString());
        res.setEndDate(p.getEndDate().toString());
        res.setQuantity(p.getQuantity());
        res.setMinimumOrderAmount(p.getMinimumOrderAmount());
        res.setActive(p.isActive());
        res.setCreatedBy(p.getCreatedBy() != null ? p.getCreatedBy().getId() : null);
        res.setApprovedBy(p.getApprovedBy() != null ? p.getApprovedBy().getId() : null);
        return res;
    }
}
