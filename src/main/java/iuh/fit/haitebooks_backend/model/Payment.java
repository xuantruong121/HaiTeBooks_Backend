package iuh.fit.haitebooks_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Method method; // CASH | VNPAY

    @Column(nullable = false)
    private double amount;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status_payment", nullable = false)
    private Status_Payment status = Status_Payment.PENDING;

    // ===== Thông tin dành riêng cho VNPay =====

    @Column(name = "vnp_txn_ref", length = 100)
    private String vnpTxnRef;

    @Column(name = "vnp_transaction_no", length = 100)
    private String vnpTransactionNo;

    @Column(name = "vnp_response_code", length = 20)
    private String vnpResponseCode;

    @Column(name = "vnp_bank_code", length = 50)
    private String vnpBankCode;

    @Column(name = "vnp_pay_date", length = 20)
    private String vnpPayDate;

    @Lob
    @Column(name = "raw_response", columnDefinition = "MEDIUMTEXT")
    private String rawResponse;
}