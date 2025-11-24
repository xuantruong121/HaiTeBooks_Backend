package iuh.fit.haitebooks_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "promotion_logs")
@Getter
@Setter
public class PromotionLog {

    public static final String CREATE = "CREATE";
    public static final String APPROVE = "APPROVE";
    public static final String REJECT = "REJECT";
    public static final String DEACTIVATE = "DEACTIVATE";
    public static final String ACTIVATE = "ACTIVATE";
    public static final String UPDATE = "UPDATE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Khuyến mãi được tác động
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    // Người thực hiện hành động (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id", nullable = false)
    private User actor;

    @Column(length = 50, nullable = false)
    private String action;

    @Column(name = "log_time", nullable = false)
    private LocalDateTime logTime;
}
