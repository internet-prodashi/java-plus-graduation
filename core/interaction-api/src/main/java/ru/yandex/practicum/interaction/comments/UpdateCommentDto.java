package ru.yandex.practicum.interaction.comments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCommentDto(
        @NotBlank(message = "Comment cannot be empty")
        @Size(min = 1, max = 5000, message = "Comment must contain from {min} to {max} characters")
        String text
) {
}
