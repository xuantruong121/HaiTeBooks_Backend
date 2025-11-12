package iuh.fit.haitebooks_backend.mapper;

import iuh.fit.haitebooks_backend.dtos.request.BookRequest;
import iuh.fit.haitebooks_backend.dtos.response.BookResponse;
import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.BookCategory;

public class BookMapper {

    public static BookResponse toBookResponse(Book book) {
        if (book == null) return null;

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

    public static Book toBookEntity(BookRequest request, BookCategory category) {
        if (request == null) return null;

        Book book = new Book();
        book.setId(request.getId());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPrice(request.getPrice());
        book.setStock(request.getStock());
        book.setDescription(request.getDescription());
        book.setImageUrl(request.getImageUrl());
        book.setBarcode(request.getBarcode());
        book.setCategory(category);
        return book;
    }

    public static void updateBookFromRequest(Book book, BookRequest request, BookCategory category) {
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPrice(request.getPrice());
        book.setStock(request.getStock());
        book.setDescription(request.getDescription());
        book.setImageUrl(request.getImageUrl());
        book.setBarcode(request.getBarcode());
        book.setCategory(category);
    }
}
