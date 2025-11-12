package iuh.fit.haitebooks_backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mapping to User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, name = "order_date")
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(nullable = false)
    private double total;

    // PENDING, PROCESSING, COMPLETED, CANCELLED, SHIPPING
    @Enumerated(EnumType.STRING)
    private Status_Order status;

    // 🏠 Địa chỉ giao hàng
    @Column(length = 255)
    private String address;

    // 📝 Ghi chú thêm của khách hàng
    @Column(length = 500)
    private String note;

    // Mapping to OrderItem
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<Order_Item> orderItems;

    // Mapping to Payment
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Payment payment;
}
