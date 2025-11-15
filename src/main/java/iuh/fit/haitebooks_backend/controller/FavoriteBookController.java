package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.request.FavoriteBookRequest;
import iuh.fit.haitebooks_backend.dtos.response.FavoriteBookResponse;
import iuh.fit.haitebooks_backend.model.FavoriteBook;
import iuh.fit.haitebooks_backend.service.FavoriteBookService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "*")
public class FavoriteBookController {

    private final FavoriteBookService favoriteBookService;

    public FavoriteBookController(FavoriteBookService favoriteBookService) {
        this.favoriteBookService = favoriteBookService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FavoriteBookResponse>> getFavoritesByUser(@PathVariable Long userId) {
        List<FavoriteBookResponse> responses = favoriteBookService.getFavoritesByUser(userId)
                .stream()
                .map(f -> new FavoriteBookResponse(
                        f.getId(),
                        f.getUser().getId(),
                        f.getBook().getId(),
                        f.getBook().getTitle(),
                        f.getBook().getAuthor(),
                        f.getBook().getPrice(),
                        f.getBook().getImageUrl(),
                        f.getCreatedAt()
                ))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/add")
    public ResponseEntity<FavoriteBookResponse> addToFavorites(@RequestBody FavoriteBookRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        FavoriteBook favoriteBook = favoriteBookService.addToFavorites(request, username);
        
        FavoriteBookResponse response = new FavoriteBookResponse(
                favoriteBook.getId(),
                favoriteBook.getUser().getId(),
                favoriteBook.getBook().getId(),
                favoriteBook.getBook().getTitle(),
                favoriteBook.getBook().getAuthor(),
                favoriteBook.getBook().getPrice(),
                favoriteBook.getBook().getImageUrl(),
                favoriteBook.getCreatedAt()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove/{bookId}")
    public ResponseEntity<Void> removeFromFavorites(@PathVariable Long bookId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        favoriteBookService.removeFromFavorites(bookId, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check/{bookId}")
    public ResponseEntity<Boolean> isFavorite(@PathVariable Long bookId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isFavorite = favoriteBookService.isFavorite(bookId, username);
        return ResponseEntity.ok(isFavorite);
    }
}
