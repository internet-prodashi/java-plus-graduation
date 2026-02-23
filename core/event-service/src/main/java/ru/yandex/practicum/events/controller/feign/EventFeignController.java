package ru.yandex.practicum.events.controller.feign;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.events.service.EventFeignService;
import ru.yandex.practicum.interaction.events.EventFullDto;
import ru.yandex.practicum.interaction.events.EventShortDto;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class EventFeignController {
    private final EventFeignService eventFeignService;

    @GetMapping("/api/events/exists-by-category-id/{cat-id}")
    @ResponseStatus(HttpStatus.OK)
    public Boolean existsByCategoryId(@PathVariable("cat-id") Long categoryId) {
        log.info("Controller feign: existsByCategoryId with parameter categoryId={} ", categoryId);
        return eventFeignService.existsByCategoryId(categoryId);
    }

    @GetMapping("/api/events/{event-id}")
    public EventFullDto getEventOrThrow(@Valid @PathVariable("event-id") Long eventId) {
        log.debug("Controller feign: getEventOrThrow  request for event with id: {}}", eventId);
        return eventFeignService.getEventOrThrow(eventId);
    }

    @GetMapping("/api/events/find-all-by-event-ids")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> findAllByEventIds(@RequestParam("eventIds") List<Long> eventIds) {
        log.info("Controller feign: findAllByEventIds");
        return eventFeignService.findAllByEventIds(eventIds);
    }
}