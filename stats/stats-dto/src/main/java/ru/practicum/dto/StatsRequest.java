package ru.practicum.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.practicum.dto.constants.DateTimeConstants.DATE_TIME_PATTERN;

public record StatsRequest(
        LocalDateTime start,
        LocalDateTime end,
        List<String> uris,
        boolean unique
) {
    public static StatsRequest of(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        return new StatsRequest(
                start,
                end,
                uris,
                unique
        );
    }
}
