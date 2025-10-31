package iuh.fit.haitebooks_backend.mapper;

import iuh.fit.haitebooks_backend.dtos.response.BookResponse;
import iuh.fit.haitebooks_backend.model.Book;

public class BookMapper {

    public static BookResponse toBookResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPrice(),
                book.getStock(),
                book.getDescription(),
                book.getImageUrl(),
                book.getBarcode(),
                book.getCategory() != null ? book.getCategory().getName() : null
        );
    }
}
