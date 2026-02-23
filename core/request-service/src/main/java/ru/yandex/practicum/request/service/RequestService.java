package ru.yandex.practicum.request.service;

import ru.yandex.practicum.interaction.request.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.interaction.request.EventRequestStatusUpdateResult;
import ru.yandex.practicum.interaction.request.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> getRequestsByEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    List<ParticipationRequestDto> getRequestsByUserId(Long userId);

    ParticipationRequestDto addRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
}
