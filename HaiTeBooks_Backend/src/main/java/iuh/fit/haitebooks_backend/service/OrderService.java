package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.model.Order;
import iuh.fit.haitebooks_backend.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    // optionally inject others like CartRepository, PaymentService

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(Order order) {
        // you may implement business logic: calculate total, set status, reduce stock...
        return orderRepository.save(order);
    }

    public List<Order> findByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order getById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Order updateOrder(Long id, Order details) {
        Order o = orderRepository.findById(id).orElseThrow();
        o.setStatus(details.getStatus());
        // set other fields if needed
        return orderRepository.save(o);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
