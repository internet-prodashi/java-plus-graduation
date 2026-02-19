package ru.yandex.practicum.interaction.request;

import ru.yandex.practicum.interaction.request.enums.RequestStatus;

import java.util.List;

public record EventRequestStatusUpdateRequest(
        List<Long> requestIds,
        RequestStatus status
) {
}