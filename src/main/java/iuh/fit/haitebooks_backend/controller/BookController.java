package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.response.BookResponse;
import iuh.fit.haitebooks_backend.mapper.BookMapper;
import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import iuh.fit.haitebooks_backend.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*") // Cho phép frontend React Native truy cập
public class BookController {

    @Autowired
    private final BookRepository bookRepository;

    private final BookService bookService;

    public BookController(BookRepository bookRepository, BookService bookService) {
        this.bookService = bookService;
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        List<BookResponse> responses = bookRepository.findAll()
                .stream()
                .map(BookMapper::toBookResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    // ✅ API quét mã sách
    @GetMapping("/barcode/{code}")
    public ResponseEntity<Book> getBookByBarcode(@PathVariable String code) {
        Book book = bookService.findByBarcode(code);

        if (book != null) {
            return ResponseEntity.ok(book);
        } else {
            // Nếu không có trong DB, có thể gọi API ngoài để lấy
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public Book createBook(@RequestBody Book book) {
        return bookRepository.save(book);
    }

    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody Book bookDetails) {
        Book book = bookRepository.findById(id).orElseThrow();
        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setPrice(bookDetails.getPrice());
        return bookRepository.save(book);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookRepository.deleteById(id);
    }
}
