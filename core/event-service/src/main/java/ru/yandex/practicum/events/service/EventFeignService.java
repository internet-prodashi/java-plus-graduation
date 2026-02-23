package ru.yandex.practicum.events.service;

import ru.yandex.practicum.interaction.events.EventFullDto;
import ru.yandex.practicum.interaction.events.EventShortDto;

import java.util.List;

public interface EventFeignService {
    Boolean existsByCategoryId(Long categoryId);

    EventFullDto getEventOrThrow(Long eventId);

    List<EventShortDto> findAllByEventIds(List<Long> eventIds);
}
