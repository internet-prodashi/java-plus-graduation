package ru.yandex.practicum.interaction.feign.clients;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.interaction.events.EventFullDto;
import ru.yandex.practicum.interaction.events.EventShortDto;
import ru.yandex.practicum.interaction.feign.config.FeignConfig;

import java.util.List;

@FeignClient(name = "event-service", configuration = {FeignConfig.class})
public interface EventFeignClient {
    @GetMapping("/api/events/exists-by-category-id/{cat-id}")
    Boolean existsByCategoryId(@PathVariable("cat-id") Long categoryId);

    @GetMapping("/api/events/{event-id}")
    EventFullDto getEventOrThrow(@Valid @PathVariable("event-id") Long eventId);

    @GetMapping("/api/events/find-all-by-event-ids")
    List<EventShortDto> findAllByEventIds(@RequestParam("eventIds") List<Long> eventIds);
}