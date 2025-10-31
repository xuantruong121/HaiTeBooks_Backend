package iuh.fit.haitebooks_backend.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private double price;
    private int stock;
    private String description;
    private String imageUrl;
    private String barcode;
    private String categoryName;
}
