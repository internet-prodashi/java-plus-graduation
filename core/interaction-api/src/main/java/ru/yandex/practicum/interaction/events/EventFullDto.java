package ru.yandex.practicum.interaction.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.yandex.practicum.interaction.category.CategoryDto;
import ru.yandex.practicum.interaction.events.enums.EventState;
import ru.yandex.practicum.interaction.user.UserShortDto;

import java.time.LocalDateTime;

public record EventFullDto(
        Long id,

        String annotation,

        CategoryDto category,

        Long confirmedRequests,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdOn,

        String description,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime eventDate,

        UserShortDto initiator,

        LocationDto location,

        Boolean paid,

        Integer participantLimit,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime publishedOn,

        Boolean requestModeration,

        EventState state,

        String title,

        Long views
) {
}
