package iuh.fit.haitebooks_backend.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {
    private Long id;

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Author cannot be blank")
    private String author;

    @NotNull(message = "Price cannot be null")
    @Min(value = 0, message = "Price must be positive")
    private Double price;

    @NotNull(message = "Stock cannot be null")
    @Min(value = 0, message = "Stock must be positive")
    private Integer stock;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    private String imageUrl;

    // ✅ Không bắt buộc — BE sẽ tự sinh nếu null
    private String barcode;

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;
}
