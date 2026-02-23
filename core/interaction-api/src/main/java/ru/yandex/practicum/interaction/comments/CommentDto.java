package ru.yandex.practicum.interaction.comments;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record CommentDto(
        Long id,

        String text,

        Long userId,

        Long eventId,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdDate
) {
}
