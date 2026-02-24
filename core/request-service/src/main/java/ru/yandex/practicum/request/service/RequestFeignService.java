package ru.yandex.practicum.request.service;

import ru.yandex.practicum.interaction.events.EventWithCountConfirmedRequests;
import ru.yandex.practicum.interaction.request.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.interaction.request.EventRequestStatusUpdateResult;
import ru.yandex.practicum.interaction.request.ParticipationRequestDto;

import java.util.List;

public interface RequestFeignService {
    List<EventWithCountConfirmedRequests> findCountConfirmedRequestsByEventIds(List<Long> eventIds);

    List<ParticipationRequestDto> getRequestsByEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    ParticipationRequestDto getUserRequest(Long userId, Long eventId);
}
