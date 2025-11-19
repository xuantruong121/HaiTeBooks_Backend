package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.CategoryRequest;
import iuh.fit.haitebooks_backend.dtos.response.CategoryResponse;
import iuh.fit.haitebooks_backend.mapper.CategoryMapper;
import iuh.fit.haitebooks_backend.model.BookCategory;
import iuh.fit.haitebooks_backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // ✅ Lấy tất cả category
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ✅ Tạo mới — kiểm tra trùng tên
    @Transactional
    public BookCategory createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Category name already exists: " + request.getName());
        }

        BookCategory category = CategoryMapper.toEntity(request);
        return categoryRepository.save(category);
    }

    // ✅ Lấy theo ID
    @Transactional(readOnly = true)
    public BookCategory getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id " + id));
    }

    // ✅ Cập nhật — kiểm tra trùng tên (trừ chính nó)
    @Transactional
    public BookCategory updateCategory(Long id, CategoryRequest request) {
        BookCategory category = getCategoryById(id);

        categoryRepository.findByNameIgnoreCase(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("Category name already exists: " + request.getName());
            }
        });

        CategoryMapper.updateEntity(category, request);
        return categoryRepository.save(category);
    }

    // ✅ Xóa
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id " + id);
        }
        categoryRepository.deleteById(id);
    }
}
