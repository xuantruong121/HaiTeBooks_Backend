package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.request.CategoryRequest;
import iuh.fit.haitebooks_backend.dtos.response.CategoryResponse;
import iuh.fit.haitebooks_backend.mapper.CategoryMapper;
import iuh.fit.haitebooks_backend.model.BookCategory;
import iuh.fit.haitebooks_backend.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // ✅ Lấy tất cả category
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> responses = categoryService.getAllCategories();
        return ResponseEntity.ok(responses);
    }

    // ✅ Tạo mới
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        BookCategory category = categoryService.createCategory(request);
        return ResponseEntity.ok(CategoryMapper.toResponse(category));
    }

    // ✅ Lấy theo ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long id) {
        BookCategory category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(CategoryMapper.toResponse(category));
    }

    // ✅ Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request
    ) {
        BookCategory updated = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(CategoryMapper.toResponse(updated));
    }

    // ✅ Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
