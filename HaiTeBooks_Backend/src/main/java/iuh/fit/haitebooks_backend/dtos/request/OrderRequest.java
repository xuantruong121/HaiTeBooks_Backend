package iuh.fit.haitebooks_backend.dtos.request;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private Long userId;
    private List<Long> bookIds; // or order items depending on model
    private String address;
    private String note;
}
