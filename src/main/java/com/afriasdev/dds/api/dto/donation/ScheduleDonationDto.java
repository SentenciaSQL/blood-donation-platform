package com.afriasdev.dds.api.dto.donation;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ScheduleDonationDto(
        @NotNull
        Long bloodBankId,
        Long requestId,
        @NotNull
        Instant scheduledAt
) {
}
