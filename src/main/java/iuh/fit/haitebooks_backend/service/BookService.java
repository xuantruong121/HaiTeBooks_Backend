package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.BookRequest;
import iuh.fit.haitebooks_backend.mapper.BookMapper;
import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.BookCategory;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import iuh.fit.haitebooks_backend.repository.CategoryRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Page<Book> getBooksWithPagination(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return (keyword != null && !keyword.isBlank())
                ? bookRepository.findByTitleContainingIgnoreCase(keyword, pageable)
                : bookRepository.findAll(pageable);
    }

    public Book findByBarcode(String barcode) {
        return bookRepository.findByBarcode(barcode).orElse(null);
    }

    // ✅ Thêm mới sách bằng BookRequest
    public Book createBook(BookRequest request) {
        BookCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id " + request.getCategoryId()));

        Book book = BookMapper.toBookEntity(request, category);
        return bookRepository.save(book);
    }

    // ✅ Lấy theo ID
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id " + id));
    }

    // ✅ Cập nhật sách
    public Book updateBook(Long id, BookRequest request) {
        Book book = getBookById(id);
        BookCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id " + request.getCategoryId()));

        BookMapper.updateBookFromRequest(book, request, category);
        return bookRepository.save(book);
    }

    // ✅ Xóa
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Book not found with id " + id);
        }
        bookRepository.deleteById(id);
    }
}
