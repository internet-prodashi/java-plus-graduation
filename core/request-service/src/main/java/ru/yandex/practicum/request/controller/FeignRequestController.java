package ru.yandex.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.events.EventWithCountConfirmedRequests;
import ru.yandex.practicum.interaction.request.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.interaction.request.EventRequestStatusUpdateResult;
import ru.yandex.practicum.interaction.request.ParticipationRequestDto;
import ru.yandex.practicum.request.service.RequestFeignService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FeignRequestController {
    private final RequestFeignService requestFeignService;

    @GetMapping("/api/requests/confirmed-count")
    public List<EventWithCountConfirmedRequests> findCountConfirmedRequestsByEventIds(
            @RequestParam("eventIds") List<Long> eventIds
    ) {
        log.info("Request to get confirmed request count whit events IDs: {}", eventIds);
        return requestFeignService.findCountConfirmedRequestsByEventIds(eventIds);
    }

    @GetMapping("/api/requests/get-user-request/user/{user-id}/event/{event-id}")
    public List<ParticipationRequestDto> getRequestsByEvent(
            @PathVariable("user-id") Long userId,
            @PathVariable("event-id") Long eventId
    ) {
        log.info("Controller: getRequestsByEvent with userId: {}, eventId: {}", userId, eventId);
        return requestFeignService.getRequestsByEvent(userId, eventId);
    }

    @PutMapping("/api/requests/update-request-status")
    public EventRequestStatusUpdateResult updateRequestStatus(
            @RequestParam("user-id") Long userId,
            @RequestParam("event-id") Long eventId,
            @RequestBody EventRequestStatusUpdateRequest request
    ) {
        log.info("Controller: updateRequestStatus with userId: {}, eventId: {}, request: {}", userId, eventId, request);
        return requestFeignService.updateRequestStatus(userId, eventId, request);
    }

    @GetMapping("/api/requests/get-request-user-and-event/user/{user-id}/event/{event-id}")
    public ParticipationRequestDto getUserRequest(
            @PathVariable("user-id") Long userId,
            @PathVariable("event-id") Long eventId
    ) {
        log.info("Controller: getUserRequest with userId: {}, eventId: {}", userId, eventId);
        return requestFeignService.getUserRequest(userId, eventId);
    }
}