package ru.yandex.practicum.interaction.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import ru.yandex.practicum.interaction.events.enums.StateActionAdmin;

import java.time.LocalDateTime;

public record UpdateEventAdminRequest(
        @Size(min = 20, max = 2000, message = "Invalid number of characters")
        String annotation,

        Long category,

        @Size(min = 20, max = 7000, message = "Description must contain from {min} to {max} characters")
        String description,

        @Future(message = "Date event should be in the future")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime eventDate,

        LocationDto location,

        Boolean paid,

        @PositiveOrZero(message = "Event participant limit must be zero or more than zero")
        Integer participantLimit,

        Boolean requestModeration,

        StateActionAdmin stateAction,

        @Size(min = 3, max = 120, message = "Title must contain from {min} to {max} characters")
        String title
) {
}
