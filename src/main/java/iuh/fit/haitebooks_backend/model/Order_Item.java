package iuh.fit.haitebooks_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_item")
public class Order_Item {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    //Mapping to Order and Book with quantity and price
    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    private Book book;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double price;
}
