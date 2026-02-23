package ru.yandex.practicum.interaction.compilation;

import ru.yandex.practicum.interaction.events.EventShortDto;

import java.util.List;

public record CompilationDto(
        Long id,
        List<EventShortDto> events,
        boolean pinned,
        String title
) {
}
