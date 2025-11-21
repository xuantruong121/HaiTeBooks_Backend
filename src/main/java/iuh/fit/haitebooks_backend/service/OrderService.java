package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.NotificationRequest;
import iuh.fit.haitebooks_backend.dtos.request.OrderRequest;
import iuh.fit.haitebooks_backend.dtos.response.OrderResponse;
import iuh.fit.haitebooks_backend.exception.BadRequestException;
import iuh.fit.haitebooks_backend.exception.NotFoundException;
import iuh.fit.haitebooks_backend.mapper.OrderMapper;
import iuh.fit.haitebooks_backend.model.*;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import iuh.fit.haitebooks_backend.repository.OrderRepository;
import iuh.fit.haitebooks_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PromotionService promotionService;

    public OrderService(OrderRepository orderRepository, BookRepository bookRepository, UserRepository userRepository,
                        NotificationService notificationService, PromotionService promotionService) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.promotionService = promotionService;
    }

    // ✅ Tạo đơn hàng mới
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(Status_Order.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setAddress(request.getAddress());
        order.setNote(request.getNote());

        // ✅ THÊM: Lưu paymentMethod từ request
        if (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank()) {
            try {
                order.setPaymentMethod(Method.valueOf(request.getPaymentMethod().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Nếu không hợp lệ, set mặc định là CASH
                log.warn("⚠️ Payment method không hợp lệ: '{}'. Sử dụng mặc định: CASH", request.getPaymentMethod());
                order.setPaymentMethod(Method.CASH);
            }
        } else {
            // Mặc định là CASH nếu không có
            order.setPaymentMethod(Method.CASH);
        }

        // Tạo biến final để sử dụng trong lambda
        final Order finalOrder = order;

        // ========================================================
        // Xử lý cart items
        // ========================================================
        List<Order_Item> items = request.getOrderItems().stream().map(itemReq -> {

            Book book = bookRepository.findById(itemReq.getBookId())
                    .orElseThrow(() -> new NotFoundException("Book not found"));

            if (book.getStock() < itemReq.getQuantity()) {
                throw new BadRequestException("Not enough stock for: " + book.getTitle());
            }

            // ✅ Tối ưu: Chỉ cập nhật stock, không save từng cái
            // Hibernate sẽ tự động flush khi transaction commit
            book.setStock(book.getStock() - itemReq.getQuantity());

            Order_Item item = new Order_Item();
            item.setOrder(finalOrder);
            item.setBook(book);
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(itemReq.getPrice());
            return item;

        }).toList();
        
        // ✅ Tối ưu: Batch update tất cả books một lần
        // Collect unique books và saveAll (nếu cần, nhưng thường Hibernate tự flush)
        // Vì đã set stock, Hibernate sẽ tự động update khi transaction commit

        order.setOrderItems(items);

        // ========================================================
        // 🔥 Tính tổng tiền TRƯỚC khi áp dụng khuyến mãi
        // ========================================================
        double total = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        // ========================================================
        // 🔥 ÁP DỤNG KHUYẾN MÃI (NẾU CÓ) - Kiểm tra điều kiện đơn hàng tối thiểu
        // ========================================================
        Promotion appliedPromotion = null;

        if (request.getPromotionCode() != null && !request.getPromotionCode().isBlank()) {
            // Truyền tổng tiền để kiểm tra điều kiện đơn hàng tối thiểu
            appliedPromotion = promotionService.applyPromotion(request.getPromotionCode(), total);
            order.setAppliedPromotion(appliedPromotion);
        }

        // ========================================================
        // 🔥 Tính tổng tiền SAU khi trừ khuyến mãi
        // ========================================================
        if (appliedPromotion != null) {
            double discount = total * (appliedPromotion.getDiscountPercent() / 100.0);
            
            // ✅ Áp dụng giới hạn giảm tối đa nếu có
            if (appliedPromotion.getMaxDiscountAmount() != null && discount > appliedPromotion.getMaxDiscountAmount()) {
                discount = appliedPromotion.getMaxDiscountAmount();
            }
            
            total = total - discount;
        }

        order.setTotal(total);

        order = orderRepository.save(order);

        // 🔥 Gửi thông báo cho user
        NotificationRequest noti = new NotificationRequest();
        noti.setReceiverId(user.getId());
        noti.setTitle("Đặt hàng thành công!");
        noti.setContent("Đơn #" + order.getId() + " đã được tạo.");
        notificationService.send(noti, null);

        // Đảm bảo lazy relationships được load trong transaction
        loadLazyRelationships(order);
        return OrderMapper.toOrderResponse(order);
    }

    // ✅ Lấy tất cả đơn hàng
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        // Map trong transaction để đảm bảo lazy relationships được load
        return orders.stream()
                .map(order -> {
                    loadLazyRelationships(order);
                    return OrderMapper.toOrderResponse(order);
                })
                .collect(Collectors.toList());
    }

    // ✅ Lấy đơn hàng theo user
    @Transactional(readOnly = true)
    public List<OrderResponse> findByUser(Long userId) {
        // Tối ưu: Bỏ existsById check, findByUserId sẽ trả về empty list nếu không có
        // Nếu cần validate user, có thể check sau khi query
        List<Order> orders = orderRepository.findByUserId(userId);
        
        // Nếu không có orders và muốn validate user tồn tại, check sau
        if (orders.isEmpty()) {
            // Optional: Có thể bỏ check này nếu không cần validate user tồn tại
            // Nếu cần validate, uncomment dòng dưới:
            // if (!userRepository.existsById(userId)) {
            //     throw new NotFoundException("User not found with id: " + userId);
            // }
        }
        
        // Map trong transaction để đảm bảo lazy relationships được load
        // Với @EntityGraph, các relationships đã được eager fetch, nhưng vẫn cần trigger load để đảm bảo
        return orders.stream()
                .map(order -> {
                    loadLazyRelationships(order);
                    return OrderMapper.toOrderResponse(order);
                })
                .collect(Collectors.toList());
    }

    // ✅ Lấy đơn hàng theo ID
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found with id " + id));
        
        // Đảm bảo lazy relationships được load trong transaction
        loadLazyRelationships(order);
        return OrderMapper.toOrderResponse(order);
    }

    // ✅ Cập nhật trạng thái đơn hàng (có kiểm tra hợp lệ)
    @Transactional
    public OrderResponse updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found with id " + id));

        try {
            Status_Order newStatus = Status_Order.valueOf(status.toUpperCase());
            Status_Order current = order.getStatus();

            // Kiểm tra luồng trạng thái hợp lệ
            if (current == Status_Order.COMPLETED || current == Status_Order.CANCELLED) {
                throw new BadRequestException("Cannot change status of completed or cancelled order");
            }

            if (current == Status_Order.PENDING && newStatus == Status_Order.COMPLETED) {
                throw new BadRequestException("Order must be processed or shipped before completing");
            }

            if (current == Status_Order.PROCESSING && newStatus == Status_Order.PENDING) {
                throw new BadRequestException("Cannot revert to pending once processing");
            }

            order.setStatus(newStatus);
            Order saved = orderRepository.save(order);

            // Đảm bảo lazy relationships được load trong transaction
            loadLazyRelationships(saved);

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
                // Không để lỗi notification phá flow chính — chỉ log
                log.warn("Không gửi được notification: {}", ex.getMessage());
            }

            return OrderMapper.toOrderResponse(saved);

        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }
    }

    // ✅ Xóa đơn hàng
    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new NotFoundException("Order not found with id " + id);
        }
        orderRepository.deleteById(id);
    }

    /**
     * Đảm bảo lazy relationships được load trong transaction
     */
    private void loadLazyRelationships(Order order) {
        // Load user relationship
        if (order.getUser() != null) {
            order.getUser().getId();
            order.getUser().getUsername();
            order.getUser().getEmail();
        }
        
        // Load order items và book relationships
        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> {
                if (item.getBook() != null) {
                    item.getBook().getId();
                    item.getBook().getTitle();
                }
            });
        }
        
        // ✅ Thêm phần này để load appliedPromotion
        if (order.getAppliedPromotion() != null) {
            order.getAppliedPromotion().getId();
            order.getAppliedPromotion().getCode();
            order.getAppliedPromotion().getName();
            order.getAppliedPromotion().getDiscountPercent();
            order.getAppliedPromotion().getMinimumOrderAmount();
            order.getAppliedPromotion().getMaxDiscountAmount();
            // Load các field cần thiết khác nếu cần
        }
    }
}
