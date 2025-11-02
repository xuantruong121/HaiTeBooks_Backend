package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.request.CartRequest;
import iuh.fit.haitebooks_backend.dtos.response.CartResponse;
import iuh.fit.haitebooks_backend.model.Cart;
import iuh.fit.haitebooks_backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart") // sửa từ carts -> cart để đồng bộ với SecurityConfig
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CartResponse>> getCartByUser(@PathVariable Long userId) {
        List<CartResponse> responses = cartService.getCartByUser(userId)
                .stream()
                .map(c -> new CartResponse(c.getId(), c.getUser().getId(), c.getBook().getId(),
                        c.getQuantity()))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(@RequestBody CartRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Cart cart = cartService.addToCart(request,username);
        CartResponse response = new CartResponse(
                cart.getId(),
                cart.getUser().getId(),
                cart.getBook().getId(),
                cart.getQuantity()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Cart> updateQuantity(@PathVariable Long id, @RequestBody CartRequest req) {
        return ResponseEntity.ok(cartService.updateQuantity(id, req.getQuantity()));
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long id) {
        cartService.removeFromCart(id);
        return ResponseEntity.noContent().build();
    }
}
