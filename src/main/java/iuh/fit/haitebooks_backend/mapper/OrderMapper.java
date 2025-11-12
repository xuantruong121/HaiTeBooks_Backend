package iuh.fit.haitebooks_backend.mapper;

import iuh.fit.haitebooks_backend.dtos.response.OrderItemResponse;
import iuh.fit.haitebooks_backend.dtos.response.OrderResponse;
import iuh.fit.haitebooks_backend.model.Order;
import iuh.fit.haitebooks_backend.model.Order_Item;

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
                items
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
}
