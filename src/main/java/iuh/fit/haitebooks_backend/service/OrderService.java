package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.NotificationRequest;
import iuh.fit.haitebooks_backend.dtos.request.OrderRequest;
import iuh.fit.haitebooks_backend.model.*;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import iuh.fit.haitebooks_backend.repository.OrderRepository;
import iuh.fit.haitebooks_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepository, BookRepository bookRepository, UserRepository userRepository,
                        NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // ✅ Tạo đơn hàng mới
    @Transactional
    public Order createOrder(OrderRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(Status_Order.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setAddress(request.getAddress());
        order.setNote(request.getNote());

        List<Order_Item> items = request.getOrderItems().stream().map(itemReq -> {

            Book book = bookRepository.findById(itemReq.getBookId())
                    .orElseThrow(() -> new RuntimeException("Book not found"));

            if (book.getStock() < itemReq.getQuantity()) {
                throw new RuntimeException("Not enough stock for: " + book.getTitle());
            }

            book.setStock(book.getStock() - itemReq.getQuantity());
            bookRepository.save(book);

            Order_Item oi = new Order_Item();
            oi.setOrder(order);
            oi.setBook(book);
            oi.setPrice(itemReq.getPrice());
            oi.setQuantity(itemReq.getQuantity());
            return oi;

        }).toList();

        order.setOrderItems(items);

        double total = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        order.setTotal(total);

        orderRepository.save(order);

        // 🔥 Gửi notification realtime cho user
        NotificationRequest noti = new NotificationRequest();
        noti.setReceiverId(user.getId());
        noti.setTitle("Đặt hàng thành công!");
        noti.setContent("Đơn hàng #" + order.getId() + " đã được tạo.");
        notificationService.send(noti, null);

        return order;
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
            Order saved = orderRepository.save(order);

            // --- Gửi notification cho user của đơn hàng ---
            try {
                Long receiverId = saved.getUser() != null ? saved.getUser().getId() : null;
                if (receiverId != null) {
                    NotificationRequest noti = new NotificationRequest();
                    noti.setReceiverId(receiverId);

                    // Tiêu đề & nội dung tuỳ theo trạng thái
                    String title = "Cập nhật trạng thái đơn hàng";
                    String content = "Đơn hàng #" + saved.getId() + " đã chuyển sang trạng thái: " + newStatus.name();

                    if (newStatus == Status_Order.CANCELLED) {
                        title = "Đơn hàng đã bị huỷ";
                        content = "Đơn hàng #" + saved.getId() + " đã bị huỷ. Vui lòng liên hệ cửa hàng để biết thêm chi tiết.";
                    } else if (newStatus == Status_Order.SHIPPING) {
                        title = "Đơn hàng đang giao";
                        content = "Đơn hàng #" + saved.getId() + " đang được giao đến địa chỉ: " + (saved.getAddress() != null ? saved.getAddress() : "");
                    } else if (newStatus == Status_Order.PROCESSING) {
                        title = "Đơn hàng đang xử lý";
                        content = "Đơn hàng #" + saved.getId() + " đang được xử lý.";
                    } else if (newStatus == Status_Order.COMPLETED) {
                        title = "Đơn hàng đã hoàn tất";
                        content = "Đơn hàng #" + saved.getId() + " đã giao thành công.";
                    }

                    noti.setTitle(title);
                    noti.setContent(content);

                    // gửi realtime + lưu DB. senderId = null (nếu muốn, controller có thể truyền adminId)
                    notificationService.send(noti, null);
                }
            } catch (Exception ex) {
                // Không để lỗi notification phá flow chính — chỉ log (ở đây ném Runtime để dev thấy)
                // Bạn có thể đổi thành logger.warn(...)
                System.err.println("Không gửi được notification: " + ex.getMessage());
            }

            return saved;

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
