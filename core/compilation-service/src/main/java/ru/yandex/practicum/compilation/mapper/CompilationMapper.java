package ru.yandex.practicum.compilation.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.interaction.compilation.CompilationDto;
import ru.yandex.practicum.interaction.compilation.NewCompilationDto;
import ru.yandex.practicum.interaction.compilation.UpdateCompilationRequest;
import ru.yandex.practicum.compilation.model.Compilation;
import ru.yandex.practicum.interaction.events.EventShortDto;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventsIds", ignore = true)
    Compilation toEntity(NewCompilationDto request);

    @Mapping(target = "events", source = "eventShortDtoList")
    CompilationDto toDto(Compilation compilation, List<EventShortDto> eventShortDtoList);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCompilationFromRequest(UpdateCompilationRequest request, @MappingTarget Compilation compilation);

    @AfterMapping
    default void copyEvents(@MappingTarget Compilation compilation, UpdateCompilationRequest request) {
        if (request.getEvents() != null) {
            compilation.setEventsIds(new ArrayList<>(request.getEvents()));
        }
    }
}
