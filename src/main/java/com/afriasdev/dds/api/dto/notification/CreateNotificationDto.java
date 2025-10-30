package com.afriasdev.dds.api.dto.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateNotificationDto(
        @NotNull
        Long userId,
        @NotBlank
        String title,
        @NotBlank
        String body
) {
}
