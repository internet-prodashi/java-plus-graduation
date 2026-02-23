package ru.yandex.practicum.interaction.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record NewCompilationDto(
        List<Long> events,

        boolean pinned,

        @NotBlank(message = "Title cannot be empty")
        @Size(min = 1, max = 50, message = "Title must contain from {min} to {max} characters")
        String title
) {
}
