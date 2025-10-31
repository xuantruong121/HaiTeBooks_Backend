package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.model.Cart;
import iuh.fit.haitebooks_backend.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {
    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public List<Cart> getCartByUser(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public Cart addToCart(Cart cart) {
        return cartRepository.save(cart);
    }

    public Cart updateQuantity(Long id, int qty) {
        Cart cart = cartRepository.findById(id).orElseThrow();
        cart.setQuantity(qty);
        return cartRepository.save(cart);
    }

    public void removeFromCart(Long id) {
        cartRepository.deleteById(id);
    }
}
