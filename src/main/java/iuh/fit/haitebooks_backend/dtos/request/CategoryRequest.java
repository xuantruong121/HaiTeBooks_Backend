package iuh.fit.haitebooks_backend.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    private Long id;

    @NotBlank(message = "Category name cannot be blank")
    private String name;

    private String description;
}
