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
        order.setOrderDate(java.time.LocalDateTime.now());
        order.setAddress(request.getAddress());
        order.setNote(request.getNote());

        // Tạo OrderItems và trừ stock
        List<Order_Item> orderItems = request.getOrderItems().stream()
                .map(itemRequest -> {
                    Book book = bookRepository.findById(itemRequest.getBookId())
                            .orElseThrow(() -> new RuntimeException("Book not found with id: " + itemRequest.getBookId()));

                    if (book.getStock() < itemRequest.getQuantity()) {
                        throw new RuntimeException("Not enough stock for book: " + book.getTitle());
                    }

                    // Trừ tồn kho
                    book.setStock(book.getStock() - itemRequest.getQuantity());
                    bookRepository.save(book);

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
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        return orderRepository.findByUserId(userId);
    }

    // ✅ Lấy đơn hàng theo ID
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id " + id));
    }

    // ✅ Cập nhật trạng thái đơn hàng (có kiểm tra hợp lệ)
    @Transactional
    public Order updateOrderStatus(Long id, String status) {
        Order order = getOrderById(id);

        try {
            Status_Order newStatus = Status_Order.valueOf(status.toUpperCase());
            Status_Order current = order.getStatus();

            // Kiểm tra luồng trạng thái hợp lệ
            if (current == Status_Order.COMPLETED || current == Status_Order.CANCELLED) {
                throw new RuntimeException("Cannot change status of completed or cancelled order");
            }

            if (current == Status_Order.PENDING && newStatus == Status_Order.COMPLETED) {
                throw new RuntimeException("Order must be processed or shipped before completing");
            }

            if (current == Status_Order.PROCESSING && newStatus == Status_Order.PENDING) {
                throw new RuntimeException("Cannot revert to pending once processing");
            }

            order.setStatus(newStatus);
            return orderRepository.save(order);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }
    }

    // ✅ Xóa đơn hàng
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found with id " + id);
        }
        orderRepository.deleteById(id);
    }
}
