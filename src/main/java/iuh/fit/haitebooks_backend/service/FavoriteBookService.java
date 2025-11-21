package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.FavoriteBookRequest;
import iuh.fit.haitebooks_backend.exception.ConflictException;
import iuh.fit.haitebooks_backend.exception.NotFoundException;
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
        List<FavoriteBook> favorites = favoriteBookRepository.findByUserId(userId);
        
        // Trigger load lazy relationships trong transaction
        favorites.forEach(favorite -> {
            if (favorite.getBook() != null) {
                favorite.getBook().getId(); // Trigger load book
                favorite.getBook().getTitle(); // Trigger load book fields
                favorite.getBook().getAuthor();
                favorite.getBook().getPrice();
                favorite.getBook().getImageUrl();
                
                // Nếu Book có category (lazy), cũng cần trigger load
                if (favorite.getBook().getCategory() != null) {
                    favorite.getBook().getCategory().getName();
                }
            }
            if (favorite.getUser() != null) {
                favorite.getUser().getId(); // Trigger load user
            }
        });
        
        return favorites;
    }

    @Transactional
    public FavoriteBook addToFavorites(FavoriteBookRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new NotFoundException("Book not found"));

        // Kiểm tra xem đã có trong favorites chưa
        Optional<FavoriteBook> existing = favoriteBookRepository.findByUserAndBook(user, book);
        if (existing.isPresent()) {
            throw new ConflictException("Book already in favorites");
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
                .orElseThrow(() -> new NotFoundException("User not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found"));

        favoriteBookRepository.deleteByUserAndBook(user, book);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long bookId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found"));

        return favoriteBookRepository.existsByUserAndBook(user, book);
    }
}
