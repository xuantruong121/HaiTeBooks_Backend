package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.request.BookRequest;
import iuh.fit.haitebooks_backend.dtos.response.BookResponse;
import iuh.fit.haitebooks_backend.mapper.BookMapper;
import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // ✅ Lấy tất cả (không phân trang)
    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        List<BookResponse> responses = bookService.getAllBooks()
                .stream().map(BookMapper::toBookResponse).toList();
        return ResponseEntity.ok(responses);
    }

    // ✅ Phân trang + filter
    @GetMapping("/page")
    public ResponseEntity<Page<BookResponse>> getBooksWithPagination(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var booksPage = bookService.getBooksWithPagination(keyword, page, size)
                .map(BookMapper::toBookResponse);
        return ResponseEntity.ok(booksPage);
    }

    // ✅ Quét barcode
    @GetMapping("/barcode/{code}")
    public ResponseEntity<BookResponse> getBookByBarcode(@PathVariable String code) {
        Book book = bookService.findByBarcode(code);
        return (book != null)
                ? ResponseEntity.ok(BookMapper.toBookResponse(book))
                : ResponseEntity.notFound().build();
    }

    // ✅ Tạo mới
    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        Book created = bookService.createBook(request);
        return ResponseEntity.ok(BookMapper.toBookResponse(created));
    }

    // ✅ Lấy theo ID
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(BookMapper.toBookResponse(book));
    }

    // ✅ Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request
    ) {
        Book updated = bookService.updateBook(id, request);
        return ResponseEntity.ok(BookMapper.toBookResponse(updated));
    }

    // ✅ Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
