package iuh.fit.haitebooks_backend.dtos.request;

import lombok.Data;

@Data
public class CartRequest {
    private Long userId;
    private Long bookId;
    private int quantity;
}
