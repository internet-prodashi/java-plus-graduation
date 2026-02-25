package ru.yandex.practicum.events.controller.publics;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.events.EventFullDto;
import ru.yandex.practicum.interaction.events.EventShortDto;
import ru.yandex.practicum.interaction.events.SearchEventPublicRequest;
import ru.yandex.practicum.events.service.EventService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventsController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventsPublic(
            @ModelAttribute @Valid SearchEventPublicRequest request,
            HttpServletRequest httpRequest
    ) {
        int size = (request.size() != null && request.size() > 0) ? request.size() : 10;
        int from = request.from() != null ? request.from() : 0;
        PageRequest pageRequest = PageRequest.of(from / size, size);
        log.debug("Controller: getEventsPublic filters={}", request);
        return eventService.getEventsPublic(request, pageRequest, httpRequest.getRemoteAddr());
    }

    @GetMapping("/{id}")
    public EventFullDto getEventByIdPublic(
            @PathVariable("id") @Positive Long eventId,
            HttpServletRequest httpRequest
    ) {
        log.debug("Controller: getEventByIdPublic eventId={}", eventId);
        return eventService.getEventByIdPublic(eventId, httpRequest.getRemoteAddr());
    }

    @GetMapping("/events/recommendation")
    public List<EventShortDto> getRecommendation(
            @RequestHeader("X-EWM-USER-ID") long userId,
            @RequestParam(required = false, defaultValue = "5") int maxResult
    ) {
        log.info("Controller: getRecommendation with userId={}", userId);
        return eventService.getRecommendation(userId, maxResult);
    }

    @PutMapping("/events/{event-id}/like")
    public void addLikeToEvent(
            @PathVariable("event-id") Long eventId,
            @RequestHeader("X-EWM-USER-ID") long userId
    ) {
        log.info("Controller: addLikeToEvent with eventId={} and userId={}", eventId, userId);
        eventService.addLikeToEvent(eventId, userId);
    }
}