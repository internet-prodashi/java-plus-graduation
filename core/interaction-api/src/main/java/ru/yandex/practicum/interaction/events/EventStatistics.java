package ru.yandex.practicum.interaction.events;

import java.util.Map;

public record EventStatistics(
        Map<Long, Integer> confirmedRequests
) {
}
