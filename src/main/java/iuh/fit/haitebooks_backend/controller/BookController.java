package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.request.BookRequest;
import iuh.fit.haitebooks_backend.dtos.response.BookResponse;
import iuh.fit.haitebooks_backend.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // ✅ Lấy tất cả (không phân trang)
    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        List<BookResponse> responses = bookService.getAllBooks();
        return ResponseEntity.ok(responses);
    }

    // ✅ Phân trang + filter
    @GetMapping("/page")
    public ResponseEntity<Page<BookResponse>> getBooksWithPagination(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<BookResponse> booksPage = bookService.getBooksWithPagination(keyword, page, size);
        return ResponseEntity.ok(booksPage);
    }

    // ✅ Lấy sách bằng barcode (VD: quét barcode)
    @GetMapping("/barcode/{code}")
    public ResponseEntity<BookResponse> getBookByBarcode(@PathVariable String code) {
        BookResponse book = bookService.findByBarcode(code);
        return (book != null)
                ? ResponseEntity.ok(book)
                : ResponseEntity.notFound().build();
    }

    // ✅ Thêm sách mới (tự sinh barcode nếu chưa có)
    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        BookResponse created = bookService.createBook(request);
        return ResponseEntity.ok(created);
    }

    // ✅ Lấy theo ID
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        BookResponse book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    // ✅ Cập nhật sách (không thay đổi barcode)
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request
    ) {
        BookResponse updated = bookService.updateBook(id, request);
        return ResponseEntity.ok(updated);
    }

    // ✅ Xóa sách
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
