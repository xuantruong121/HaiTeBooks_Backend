package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.response.StatisticResponse;
import iuh.fit.haitebooks_backend.model.Payment;
import iuh.fit.haitebooks_backend.repository.OrderRepository;
import iuh.fit.haitebooks_backend.repository.PaymentRepository;
import iuh.fit.haitebooks_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class StatisticService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public StatisticService(UserRepository userRepository, OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    public StatisticResponse getOverview() {
        long users = userRepository.count();
        long orders = orderRepository.count();
        double revenue = paymentRepository.findAll()
                .stream()
                .mapToDouble(Payment::getAmount)
                .sum();
        return new StatisticResponse(users, orders, revenue);
    }
}
