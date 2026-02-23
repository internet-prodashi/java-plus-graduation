package ru.yandex.practicum.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.interaction.user.UserShortDto;
import ru.yandex.practicum.user.service.UserFeignService;
import ru.yandex.practicum.interaction.user.UserDto;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class FeignUserController {
    private final UserFeignService userFeignService;

    @GetMapping("/api/users/{user-id}")
    public UserDto getUserByIdOrThrow(@Valid @PathVariable("user-id") Long userId) {
        log.debug("UserFeignService: getUserByIdOrThrow userId: {}}", userId);
        return userFeignService.getUserByIdOrThrow(userId);
    }

    @GetMapping("/api/users/get-user-short-dto/{user-id}")
    public UserShortDto getUserShortDtoById(@Valid @PathVariable("user-id") Long userId) {
        log.debug("UserFeignService: getUserShortDtoById userId: {}}", userId);
        return userFeignService.getUserShortDtoById(userId);
    }
}