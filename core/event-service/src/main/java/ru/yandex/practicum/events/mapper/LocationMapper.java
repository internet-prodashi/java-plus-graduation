package ru.yandex.practicum.events.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.interaction.events.LocationDto;
import ru.yandex.practicum.events.model.EventLocation;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    EventLocation toEventLocation(LocationDto dto);

    LocationDto toDto(EventLocation entity);
}
