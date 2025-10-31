package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book findByBarcode(String barcode) {
        return bookRepository.findByBarcode(barcode).orElse(null);
    }
}

