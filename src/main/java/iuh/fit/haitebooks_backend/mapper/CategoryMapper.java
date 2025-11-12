package iuh.fit.haitebooks_backend.mapper;

import iuh.fit.haitebooks_backend.dtos.request.CategoryRequest;
import iuh.fit.haitebooks_backend.dtos.response.CategoryResponse;
import iuh.fit.haitebooks_backend.model.BookCategory;

public class CategoryMapper {

    public static CategoryResponse toResponse(BookCategory category) {
        if (category == null) return null;
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }

    public static BookCategory toEntity(CategoryRequest request) {
        if (request == null) return null;

        BookCategory category = new BookCategory();
        category.setId(request.getId());
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return category;
    }

    public static void updateEntity(BookCategory category, CategoryRequest request) {
        category.setName(request.getName());
        category.setDescription(request.getDescription());
    }
}
