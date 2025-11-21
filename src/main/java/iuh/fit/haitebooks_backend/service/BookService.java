package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.ai.service.EmbeddingAsyncService;
import iuh.fit.haitebooks_backend.dtos.request.BookRequest;
import iuh.fit.haitebooks_backend.dtos.response.BookResponse;
import iuh.fit.haitebooks_backend.exception.NotFoundException;
import iuh.fit.haitebooks_backend.mapper.BookMapper;
import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.BookCategory;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import iuh.fit.haitebooks_backend.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final EmbeddingAsyncService embeddingAsyncService;

    public BookService(BookRepository bookRepository, 
                      CategoryRepository categoryRepository,
                      EmbeddingAsyncService embeddingAsyncService) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.embeddingAsyncService = embeddingAsyncService;
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {
        // Với @EntityGraph trong repository, category đã được eager fetch
        List<Book> books = bookRepository.findAll();
        // Map trong transaction - category đã được load bởi @EntityGraph
        return books.stream()
                .map(BookMapper::toBookResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> getBooksWithPagination(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Book> booksPage = (keyword != null && !keyword.isBlank())
                ? bookRepository.findByTitleContainingIgnoreCase(keyword, pageable)
                : bookRepository.findAll(pageable);
        
        // Với @EntityGraph trong repository, category đã được eager fetch
        List<BookResponse> responses = booksPage.getContent().stream()
                .map(BookMapper::toBookResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(responses, pageable, booksPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public BookResponse findByBarcode(String barcode) {
        // Với @EntityGraph trong repository, category đã được eager fetch
        Book book = bookRepository.findByBarcode(barcode).orElse(null);
        if (book == null) return null;
        return BookMapper.toBookResponse(book);
    }

    // ✅ Thêm mới sách và tự sinh barcode hợp lệ
    @Transactional
    public BookResponse createBook(BookRequest request) {
        BookCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id " + request.getCategoryId()));

        Book book = BookMapper.toBookEntity(request, category);

        // ✅ Tự động sinh barcode duy nhất
        String barcode = generateUniqueEAN13Barcode();
        book.setBarcode(barcode);

        book = bookRepository.save(book);
        
        // Category đã được set trực tiếp, không cần trigger load
        // ✅ Tự động tạo embedding cho sách mới (chạy async để không block response)
        embeddingAsyncService.generateEmbeddingForBookAsync(book);

        return BookMapper.toBookResponse(book);
    }

    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        // Với @EntityGraph trong repository, category đã được eager fetch
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found with id " + id));
        return BookMapper.toBookResponse(book);
    }

    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found with id " + id));
        BookCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id " + request.getCategoryId()));

        BookMapper.updateBookFromRequest(book, request, category);
        book = bookRepository.save(book);
        
        // Category đã được set trực tiếp trong updateBookFromRequest, không cần trigger load
        return BookMapper.toBookResponse(book);
    }

    @Transactional
    public void deleteBook(Long id) {
        // Tối ưu: Dùng findById().orElseThrow() để tránh 2 queries
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found with id " + id));
        bookRepository.delete(book);
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
