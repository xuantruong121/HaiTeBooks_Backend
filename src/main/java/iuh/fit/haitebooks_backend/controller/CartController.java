package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.request.CartRequest;
import iuh.fit.haitebooks_backend.dtos.response.CartResponse;
import iuh.fit.haitebooks_backend.model.Cart;
import iuh.fit.haitebooks_backend.repository.CartRepository;
import iuh.fit.haitebooks_backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;
    private final CartRepository cartRepository;

    public CartController(CartService cartService, CartRepository cartRepository) {
        this.cartService = cartService;
        this.cartRepository = cartRepository;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CartResponse>> getCartByUser(@PathVariable Long userId) {
        List<CartResponse> responses = cartService.getCartByUser(userId)
                .stream()
                .map(c -> new CartResponse(c.getId(), c.getUser().getId(), c.getBook().getId(), c.getQuantity()))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<Cart> addToCart(@RequestBody Cart cart) {
        return ResponseEntity.ok(cartService.addToCart(cart));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cart> updateQuantity(@PathVariable Long id, @RequestBody CartRequest req) {
        return ResponseEntity.ok(cartService.updateQuantity(id, req.getQuantity()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long id) {
        cartService.removeFromCart(id);
        return ResponseEntity.noContent().build();
    }
}
