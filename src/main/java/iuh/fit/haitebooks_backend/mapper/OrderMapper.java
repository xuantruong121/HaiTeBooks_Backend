package iuh.fit.haitebooks_backend.mapper;

import iuh.fit.haitebooks_backend.dtos.response.OrderItemResponse;
import iuh.fit.haitebooks_backend.dtos.response.OrderResponse;
import iuh.fit.haitebooks_backend.dtos.response.PromotionResponse;
import iuh.fit.haitebooks_backend.model.Order;
import iuh.fit.haitebooks_backend.model.Order_Item;
import iuh.fit.haitebooks_backend.model.Promotion;

import java.util.List;

public class OrderMapper {

    public static OrderResponse toOrderResponse(Order order) {
        if (order == null) return null;

        List<OrderItemResponse> items = order.getOrderItems() != null
                ? order.getOrderItems().stream()
                .map(OrderMapper::toOrderItemResponse)
                .toList()
                : List.of();

        return new OrderResponse(
                order.getId(),
                order.getUser() != null ? order.getUser().getId() : null,
                order.getUser() != null ? order.getUser().getUsername() : null,
                order.getUser() != null ? order.getUser().getEmail() : null,
                order.getTotal(),
                order.getStatus() != null ? order.getStatus().name() : null,
                order.getOrderDate(),
                order.getAddress(),
                order.getNote(),
                items,
                toPromotionResponse(order.getAppliedPromotion())
        );
    }

    private static OrderItemResponse toOrderItemResponse(Order_Item item) {
        return new OrderItemResponse(
                item.getBook().getId(),
                item.getBook().getTitle(),
                item.getQuantity(),
                item.getPrice()
        );
    }

    // ✅ Thêm method này để map Promotion sang PromotionResponse
    private static PromotionResponse toPromotionResponse(Promotion promotion) {
        if (promotion == null) return null;

        PromotionResponse response = new PromotionResponse();
        response.setId(promotion.getId());
        response.setName(promotion.getName());
        response.setCode(promotion.getCode());
        response.setDiscountPercent(promotion.getDiscountPercent());
        response.setStartDate(promotion.getStartDate() != null ? promotion.getStartDate().toString() : null);
        response.setEndDate(promotion.getEndDate() != null ? promotion.getEndDate().toString() : null);
        response.setQuantity(promotion.getQuantity());
        response.setMinimumOrderAmount(promotion.getMinimumOrderAmount());
        response.setMaxDiscountAmount(promotion.getMaxDiscountAmount());
        response.setActive(promotion.isActive());
        response.setCreatedBy(promotion.getCreatedBy() != null ? promotion.getCreatedBy().getId() : null);
        response.setApprovedBy(promotion.getApprovedBy() != null ? promotion.getApprovedBy().getId() : null);

        return response;
    }
}
