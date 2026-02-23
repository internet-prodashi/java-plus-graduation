package ru.yandex.practicum.user.service;

import ru.yandex.practicum.interaction.user.UserDto;
import ru.yandex.practicum.interaction.user.UserShortDto;

public interface UserFeignService {
    UserDto getUserByIdOrThrow(Long userId);

    UserShortDto getUserShortDtoById(Long userId);
}
