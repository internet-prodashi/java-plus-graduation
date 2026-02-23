package ru.yandex.practicum.interaction.feign.clients;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.interaction.feign.config.FeignConfig;
import ru.yandex.practicum.interaction.user.UserDto;
import ru.yandex.practicum.interaction.user.UserShortDto;

@FeignClient(name = "user-service", configuration = {FeignConfig.class})
public interface UserFeignClient {
    @GetMapping("/api/users/{user-id}")
    UserDto getUserByIdOrThrow(@Valid @PathVariable("user-id") Long userId);

    @GetMapping("/api/users/get-user-short-dto/{user-id}")
    UserShortDto getUserShortDtoById(@Valid @PathVariable("user-id") Long userId);
}