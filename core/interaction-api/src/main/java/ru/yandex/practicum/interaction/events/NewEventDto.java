package ru.yandex.practicum.interaction.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record NewEventDto(
        @NotBlank(message = "Annotation cannot be empty")
        @Size(min = 20, max = 2000, message = "Annotation must contain from {min} to {max} characters")
        String annotation,

        @NotNull(message = "ID category cannot be null")
        Long category,

        @NotBlank(message = "Description cannot be empty")
        @Size(min = 20, max = 7000, message = "Description must contain from {min} to {max} characters")
        String description,

        @NotNull(message = "Event date cannot be null")
        @Future(message = "Event date should be in the future")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime eventDate,

        @NotNull(message = "Location cannot be empty")
        LocationDto location,

        Boolean paid,

        @PositiveOrZero(message = "Participant limit must be zero or more than zero")
        Integer participantLimit,

        Boolean requestModeration,

        @NotBlank(message = "Title cannot be empty")
        @Size(min = 3, max = 120, message = "Title must contain from {min} to {max} characters")
        String title
) {
    public NewEventDto {
        if (paid == null) {
            paid = Boolean.FALSE;
        }
        if (participantLimit == null) {
            participantLimit = 0;
        }
        if (requestModeration == null) {
            requestModeration = Boolean.TRUE;
        }
    }
}
