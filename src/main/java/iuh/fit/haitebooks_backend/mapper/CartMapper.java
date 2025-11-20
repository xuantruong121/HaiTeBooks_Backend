package iuh.fit.haitebooks_backend.mapper;

import iuh.fit.haitebooks_backend.dtos.response.CartResponse;
import iuh.fit.haitebooks_backend.model.Cart;

public class CartMapper {

    public static CartResponse toResponse(Cart cart) {
        if (cart == null) return null;

        return new CartResponse(
                cart.getId(),
                cart.getUser() != null ? cart.getUser().getId() : null,
                cart.getBook() != null ? cart.getBook().getId() : null,
                cart.getQuantity()
        );
    }
}

