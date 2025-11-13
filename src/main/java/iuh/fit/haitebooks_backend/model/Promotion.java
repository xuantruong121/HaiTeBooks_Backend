package iuh.fit.haitebooks_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "promotions")
@Getter
@Setter
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String name;

    @Column(length = 50, nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private double discountPercent;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // Người tạo (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    // Người duyệt (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    // Các đơn hàng đã áp dụng khuyến mãi
    @OneToMany(mappedBy = "appliedPromotion", fetch = FetchType.LAZY)
    private List<Order> orders;
}
