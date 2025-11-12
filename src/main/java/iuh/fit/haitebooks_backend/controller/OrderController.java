package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.request.OrderRequest;
import iuh.fit.haitebooks_backend.dtos.response.BookResponse;
import iuh.fit.haitebooks_backend.dtos.response.OrderResponse;
import iuh.fit.haitebooks_backend.mapper.BookMapper;
import iuh.fit.haitebooks_backend.mapper.OrderMapper;
import iuh.fit.haitebooks_backend.model.Order;
import iuh.fit.haitebooks_backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ✅ Lấy tất cả orders
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> responses = orderService.getAllOrders()
                .stream().map(OrderMapper::toOrderResponse).toList();
        return ResponseEntity.ok(responses);
    }

    // ✅ Tạo đơn hàng
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.ok(OrderMapper.toOrderResponse(order));
    }

    // ✅ Lấy đơn hàng theo user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(@PathVariable Long userId) {
        List<OrderResponse> responses = orderService.findByUser(userId)
                .stream()
                .map(OrderMapper::toOrderResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // ✅ Lấy đơn hàng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(OrderMapper.toOrderResponse(order));
    }

    // ✅ Cập nhật trạng thái đơn hàng
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id, @RequestBody Order details) {
        Order updated = orderService.updateOrder(id, details);
        return ResponseEntity.ok(OrderMapper.toOrderResponse(updated));
    }

    // ✅ Xóa đơn hàng
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
