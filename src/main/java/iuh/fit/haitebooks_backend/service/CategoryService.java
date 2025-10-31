package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.response.CategoryResponse;
import iuh.fit.haitebooks_backend.model.BookCategory;
import iuh.fit.haitebooks_backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse toResponse(BookCategory category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }

    public BookCategory updateCategory(Long id, BookCategory newCategory) {
        BookCategory category = categoryRepository.findById(id).orElseThrow();
        category.setName(newCategory.getName());
        category.setDescription(newCategory.getDescription());
        return categoryRepository.save(category);
    }
}
