package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.CartRequest;
import iuh.fit.haitebooks_backend.dtos.response.CartResponse;
import iuh.fit.haitebooks_backend.exception.NotFoundException;
import iuh.fit.haitebooks_backend.mapper.CartMapper;
import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.Cart;
import iuh.fit.haitebooks_backend.model.User;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import iuh.fit.haitebooks_backend.repository.CartRepository;
import iuh.fit.haitebooks_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public List<CartResponse> getCartByUser(Long userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);
        // Map trong transaction để đảm bảo lazy relationships được load
        return carts.stream()
                .map(cart -> {
                    loadLazyRelationships(cart);
                    return CartMapper.toResponse(cart);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public CartResponse addToCart(CartRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new NotFoundException("Book not found"));

        Optional<Cart> existingCart = cartRepository.findByUserAndBook(user, book);

        Cart cart;
        if (existingCart.isPresent()) {
            cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + request.getQuantity());
        } else {
            cart = new Cart();
            cart.setUser(user);
            cart.setBook(book);
            cart.setQuantity(request.getQuantity());
        }

        cart = cartRepository.save(cart);
        
        // Đảm bảo lazy relationships được load trong transaction
        loadLazyRelationships(cart);
        return CartMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse updateQuantity(Long id, int quantity) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cart item not found with id: " + id));
        cart.setQuantity(quantity);
        cart = cartRepository.save(cart);
        
        // Đảm bảo lazy relationships được load trong transaction
        loadLazyRelationships(cart);
        return CartMapper.toResponse(cart);
    }

    /**
     * Đảm bảo lazy relationships được load trong transaction
     * Với @EntityGraph trong repository, các relationships đã được eager fetch
     * Nhưng vẫn trigger load để đảm bảo an toàn
     */
    private void loadLazyRelationships(Cart cart) {
        if (cart.getUser() != null) {
            cart.getUser().getId();
        }
        if (cart.getBook() != null) {
            cart.getBook().getId();
            // CartResponse chỉ cần bookId, không cần các field khác của Book
            // Nếu sau này cần thêm thông tin Book vào CartResponse, uncomment:
            // cart.getBook().getTitle();
            // cart.getBook().getPrice();
        }
    }

    @Transactional
    public void removeFromCart(Long id) {
        cartRepository.deleteById(id);
    }
}
