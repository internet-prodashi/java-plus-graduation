package ru.yandex.practicum.category.service;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.interaction.category.CategoryDto;
import ru.yandex.practicum.interaction.category.NewCategoryDto;
import ru.yandex.practicum.category.model.Category;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long categoryId);

    CategoryDto updateCategory(Long categoryId, NewCategoryDto newCategoryDto);

    List<CategoryDto> getCategories(Pageable pageable);

    CategoryDto getCategoryById(Long categoryId);

    Category getCategoryByIdOrThrow(Long categoryId);
}
