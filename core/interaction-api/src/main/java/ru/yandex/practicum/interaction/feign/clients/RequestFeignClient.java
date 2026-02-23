package ru.yandex.practicum.interaction.feign.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.events.EventWithCountConfirmedRequests;
import ru.yandex.practicum.interaction.feign.config.FeignConfig;
import ru.yandex.practicum.interaction.request.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.interaction.request.EventRequestStatusUpdateResult;
import ru.yandex.practicum.interaction.request.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "request-service", configuration = FeignConfig.class)
public interface RequestFeignClient {
    @GetMapping("/api/requests/confirmed-count")
    List<EventWithCountConfirmedRequests> findCountConfirmedRequestsByEventIds(@RequestParam("eventIds") List<Long> eventIds);

    @GetMapping("/api/requests/get-user-request/user/{user-id}/event/{event-id}")
    List<ParticipationRequestDto> getRequestsByEvent(
            @PathVariable("user-id") Long userId,
            @PathVariable("event-id") Long eventId
    );

    @PutMapping(value = "/api/requests/update-request-status", consumes = MediaType.APPLICATION_JSON_VALUE)
    EventRequestStatusUpdateResult updateRequestStatus(
            @RequestParam("user-id") Long userId,
            @RequestParam("event-id") Long eventId,
            @RequestBody EventRequestStatusUpdateRequest request
    );
}