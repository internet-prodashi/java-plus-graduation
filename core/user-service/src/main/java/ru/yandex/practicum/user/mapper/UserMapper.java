package ru.yandex.practicum.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.interaction.user.NewUserRequest;
import ru.yandex.practicum.interaction.user.UserDto;
import ru.yandex.practicum.interaction.user.UserShortDto;
import ru.yandex.practicum.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    User toEntity(NewUserRequest dto);

    UserDto toDto(User user);

    UserShortDto toShortDto(User user);
}