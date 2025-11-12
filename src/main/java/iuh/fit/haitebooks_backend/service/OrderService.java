package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.OrderRequest;
import iuh.fit.haitebooks_backend.mapper.OrderMapper;
import iuh.fit.haitebooks_backend.model.*;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import iuh.fit.haitebooks_backend.repository.OrderRepository;
import iuh.fit.haitebooks_backend.repository.UserRepository;
import org.aspectj.weaver.ast.Or;
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
        // Kiểm tra user tồn tại
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id " + request.getUserId()));

        // Lấy danh sách sách từ DB
        List<Book> books = bookRepository.findAllById(request.getBookIds());
        if (books.isEmpty()) {
            throw new RuntimeException("No books found for given IDs");
        }

        // Tính tổng tiền
        double total = books.stream().mapToDouble(Book::getPrice).sum();

        // Tạo đối tượng Order
        Order order = new Order();
        order.setUser(user);
        order.setTotal(total);
        order.setStatus(Status_Order.PENDING);
        order.setOrderDate(java.time.LocalDateTime.now());

        // Tạo danh sách Order_Item
        List<Order_Item> orderItems = books.stream().map(book -> {
            Order_Item item = new Order_Item();
            item.setOrder(order);
            item.setBook(book);
            item.setQuantity(1); // default = 1, có thể truyền trong request sau này
            item.setPrice(book.getPrice());
            return item;
        }).collect(Collectors.toList());

        order.setOrderItems(orderItems);
        return orderRepository.save(order);
    }

    // ✅ Lấy danh sách toàn bộ đơn hàng
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // ✅ Lấy danh sách đơn hàng theo User
    public List<Order> findByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    // ✅ Lấy đơn hàng theo ID
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id " + id));
    }

    // ✅ Cập nhật đơn hàng
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
