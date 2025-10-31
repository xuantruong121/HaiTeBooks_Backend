package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.CartRequest;
import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.Cart;
import iuh.fit.haitebooks_backend.model.User;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import iuh.fit.haitebooks_backend.repository.CartRepository;
import iuh.fit.haitebooks_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public CartService(CartRepository cartRepository,
                       UserRepository userRepository,
                       BookRepository bookRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    public List<Cart> getCartByUser(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public Cart addToCart(CartRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setBook(book);
        cart.setQuantity(request.getQuantity());

        return cartRepository.save(cart);
    }

    public Cart updateQuantity(Long id, int quantity) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + id));
        cart.setQuantity(quantity);
        return cartRepository.save(cart);
    }

    public void removeFromCart(Long id) {
        cartRepository.deleteById(id);
    }
}
