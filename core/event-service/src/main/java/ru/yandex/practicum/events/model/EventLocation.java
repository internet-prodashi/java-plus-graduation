package ru.yandex.practicum.events.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class EventLocation {
    private Float lat;

    private Float lon;
}
