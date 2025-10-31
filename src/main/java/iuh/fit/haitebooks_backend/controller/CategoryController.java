package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.response.CategoryResponse;
import iuh.fit.haitebooks_backend.model.BookCategory;
import iuh.fit.haitebooks_backend.repository.CategoryRepository;
import iuh.fit.haitebooks_backend.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    public CategoryController(CategoryRepository categoryRepository, CategoryService categoryService) {
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> responses = categoryRepository.findAll()
                .stream()
                .map(categoryService::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<BookCategory> createCategory(@RequestBody BookCategory category) {
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookCategory> getCategory(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookCategory> updateCategory(@PathVariable Long id, @RequestBody BookCategory newCategory) {
        return ResponseEntity.ok(categoryService.updateCategory(id, newCategory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}