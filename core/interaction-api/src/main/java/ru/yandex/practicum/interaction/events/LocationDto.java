package ru.yandex.practicum.interaction.events;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LocationDto {
       @NotNull(message = "Latitude cannot be null")
       Float lat;

       @NotNull(message = "Longitude cannot be null")
       Float lon;
}
