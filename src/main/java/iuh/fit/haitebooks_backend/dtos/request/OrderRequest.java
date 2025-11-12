package iuh.fit.haitebooks_backend.dtos.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "Book IDs cannot be empty")
    private List<Long> bookIds;

    private String address;
    private String note;
}
