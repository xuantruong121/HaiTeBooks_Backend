package iuh.fit.haitebooks_backend.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long bookId;
    private String bookTitle;
    private int quantity;
    private double price;
}
