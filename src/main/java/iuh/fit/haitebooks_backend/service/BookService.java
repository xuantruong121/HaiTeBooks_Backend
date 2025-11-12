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
import java.util.Random;

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

    // ✅ Thêm mới sách và tự sinh barcode hợp lệ
    public Book createBook(BookRequest request) {
        BookCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id " + request.getCategoryId()));

        Book book = BookMapper.toBookEntity(request, category);

        // ✅ Tự động sinh barcode duy nhất
        String barcode = generateUniqueEAN13Barcode();
        book.setBarcode(barcode);

        return bookRepository.save(book);
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id " + id));
    }

    public Book updateBook(Long id, BookRequest request) {
        Book book = getBookById(id);
        BookCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id " + request.getCategoryId()));

        BookMapper.updateBookFromRequest(book, request, category);
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Book not found with id " + id);
        }
        bookRepository.deleteById(id);
    }

    // 🧮 Sinh mã barcode chuẩn EAN-13 (13 số, có checksum)
    private String generateEAN13Barcode() {
        Random random = new Random();

        // 12 số đầu ngẫu nhiên
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(random.nextInt(10));
        }

        // Tính checksum (số thứ 13)
        int checksum = calculateEAN13Checksum(sb.toString());
        sb.append(checksum);

        return sb.toString();
    }

    private int calculateEAN13Checksum(String code12) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(code12.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int mod = sum % 10;
        return (mod == 0) ? 0 : 10 - mod;
    }

    // 🔄 Đảm bảo barcode không trùng trong DB
    private String generateUniqueEAN13Barcode() {
        String barcode;
        do {
            barcode = generateEAN13Barcode();
        } while (bookRepository.findByBarcode(barcode).isPresent());
        return barcode;
    }
}
