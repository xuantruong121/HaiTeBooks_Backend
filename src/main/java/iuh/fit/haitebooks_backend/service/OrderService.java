package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.OrderRequest;
import iuh.fit.haitebooks_backend.model.*;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import iuh.fit.haitebooks_backend.repository.OrderRepository;
import iuh.fit.haitebooks_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    // ✅ Tạo đơn hàng mới
    @Transactional
    public Order createOrder(OrderRequest request) {
        // Kiểm tra user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        // Tạo Order entity
        Order order = new Order();
        order.setUser(user);
        order.setStatus(Status_Order.PENDING);
        order.setTotal(0); // tính lại bên dưới
        order.setOrderDate(java.time.LocalDateTime.now());

        // Tạo OrderItems
        List<Order_Item> orderItems = request.getOrderItems().stream()
                .map(itemRequest -> {
                    // Tìm Book
                    Book book = bookRepository.findById(itemRequest.getBookId())
                            .orElseThrow(() -> new RuntimeException("Book not found with id: " + itemRequest.getBookId()));

                    // Tạo Order_Item
                    Order_Item item = new Order_Item();
                    item.setBook(book);
                    item.setQuantity(itemRequest.getQuantity());
                    item.setPrice(itemRequest.getPrice());
                    item.setOrder(order);
                    return item;
                })
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);

        // Tính tổng tiền
        double total = orderItems.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
        order.setTotal(total);

        return orderRepository.save(order);
    }

    // ✅ Lấy tất cả đơn hàng
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // ✅ Lấy đơn hàng theo user
    public List<Order> findByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    // ✅ Lấy đơn hàng theo ID
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id " + id));
    }

    // ✅ Cập nhật trạng thái đơn hàng
    @Transactional
    public Order updateOrder(Long id, Order details) {
        Order existing = getOrderById(id);

        if (details.getStatus() != null) {
            existing.setStatus(details.getStatus());
        }
        if (details.getTotal() > 0) {
            existing.setTotal(details.getTotal());
        }

        return orderRepository.save(existing);
    }

    // ✅ Xóa đơn hàng
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found with id " + id);
        }
        orderRepository.deleteById(id);
    }
}
