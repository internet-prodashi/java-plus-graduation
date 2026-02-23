package ru.yandex.practicum.interaction.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.yandex.practicum.interaction.category.CategoryDto;
import ru.yandex.practicum.interaction.user.UserShortDto;

import java.time.LocalDateTime;

public record EventShortDto(
        Long id,

        String annotation,

        CategoryDto category,

        Long confirmedRequests,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime eventDate,

        UserShortDto initiator,

        boolean paid,

        String title,

        Long views
) {
}
