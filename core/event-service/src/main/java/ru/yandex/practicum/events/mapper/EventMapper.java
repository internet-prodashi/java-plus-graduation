package ru.yandex.practicum.events.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.interaction.category.CategoryDto;
import ru.yandex.practicum.interaction.events.*;
import ru.yandex.practicum.events.model.Event;
import ru.yandex.practicum.interaction.events.enums.EventState;
import ru.yandex.practicum.interaction.user.UserDto;
import ru.yandex.practicum.interaction.user.UserShortDto;

@Mapper(componentModel = "spring", uses = {LocationMapper.class})
public interface EventMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "initiatorId", expression = "java(initiator != null ? initiator.id() : null)")
    @Mapping(target = "categoryId", expression = "java(category != null ? category.id() : null)")
    Event fromNewEvent(NewEventDto dto, UserDto initiator, CategoryDto category, EventState state);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "initiator", expression = "java(userShortDto != null ? userShortDto : null)")
    @Mapping(target = "category", expression = "java(categoryDto != null ? categoryDto : null)")
    EventShortDto toEventShortDto(Event event, UserShortDto userShortDto, CategoryDto categoryDto, Integer confirmedRequests, Double rating);

    @Named("toEventShortWithoutStats")
    @Mapping(target = "confirmedRequests", ignore = true)
    EventShortDto toEventShortWithoutStats(Event event);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "initiator", expression = "java(userShortDto != null ? userShortDto : null)")
    @Mapping(target = "category", expression = "java(categoryDto != null ? categoryDto : null)")
    EventFullDto toEventFullDto(Event event, UserShortDto userShortDto, CategoryDto categoryDto, Integer confirmedRequests, Double rating);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "categoryId", source = "category.id")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEventFromUserRequest(UpdateEventUserRequest request, @MappingTarget Event event, CategoryDto category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "categoryId", source = "category.id")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEventFromAdminRequest(UpdateEventAdminRequest request, @MappingTarget Event event, CategoryDto category);
}
