package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.FavoriteBookRequest;
import iuh.fit.haitebooks_backend.model.Book;
import iuh.fit.haitebooks_backend.model.FavoriteBook;
import iuh.fit.haitebooks_backend.model.User;
import iuh.fit.haitebooks_backend.repository.BookRepository;
import iuh.fit.haitebooks_backend.repository.FavoriteBookRepository;
import iuh.fit.haitebooks_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FavoriteBookService {

    private final FavoriteBookRepository favoriteBookRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public FavoriteBookService(FavoriteBookRepository favoriteBookRepository,
                               UserRepository userRepository,
                               BookRepository bookRepository) {
        this.favoriteBookRepository = favoriteBookRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public List<FavoriteBook> getFavoritesByUser(Long userId) {
        return favoriteBookRepository.findByUserId(userId);
    }

    @Transactional
    public FavoriteBook addToFavorites(FavoriteBookRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // Kiểm tra xem đã có trong favorites chưa
        Optional<FavoriteBook> existing = favoriteBookRepository.findByUserAndBook(user, book);
        if (existing.isPresent()) {
            throw new RuntimeException("Book already in favorites");
        }

        FavoriteBook favoriteBook = FavoriteBook.builder()
                .user(user)
                .book(book)
                .createdAt(java.time.LocalDateTime.now())
                .build();

        return favoriteBookRepository.save(favoriteBook);
    }

    @Transactional
    public void removeFromFavorites(Long bookId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        favoriteBookRepository.deleteByUserAndBook(user, book);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long bookId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        return favoriteBookRepository.existsByUserAndBook(user, book);
    }
}
