package ru.yandex.practicum.interaction.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewUserRequest(
        @NotBlank(message = "User name cannot be empty")
        @Size(min = 2, max = 250)
        String name,

        @NotBlank(message = "User email cannot be empty")
        @Email(message = "User email was entered incorrectly")
        @Size(min = 6, max = 254)
        String email
) {
}
