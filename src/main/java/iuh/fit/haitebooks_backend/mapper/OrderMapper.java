package iuh.fit.haitebooks_backend.mapper;

import iuh.fit.haitebooks_backend.dtos.response.OrderResponse;
import iuh.fit.haitebooks_backend.model.Order;

public class OrderMapper {

    public static OrderResponse toOrderResponse(Order order) {
        if (order == null) return null;

        return new OrderResponse(
                order.getId(),
                order.getUser() != null ? order.getUser().getId() : null,
                order.getTotal(),
                order.getStatus() != null ? order.getStatus().name() : null,
                order.getOrderDate()
        );
    }
}
