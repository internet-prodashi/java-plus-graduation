package ru.yandex.practicum.interaction.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewCategoryDto(
        @NotBlank(message = "Category name cannot be empty")
        @Size(min = 1, max = 50, message = "Category name must contain from {min} to {max} characters")
        String name
) {
}
