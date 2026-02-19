package ru.yandex.practicum.interaction.exception;

public record ErrorResponse(
        String path,
        String httpMethod,
        int statusCode,
        String status,
        String error,
        String message
) {
}
