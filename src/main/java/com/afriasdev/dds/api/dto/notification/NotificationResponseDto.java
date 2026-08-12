package com.afriasdev.dds.api.dto.notification;

import java.time.Instant;

public record NotificationResponseDto(
        Long id,
        Long userId,
        String title,
        String body,
        Boolean seen,
        Instant createdAt
) {
}
