package com.afriasdev.dds.api.dto.donation;

import jakarta.validation.constraints.NotBlank;

public record UpdateDonationStatusDto(
        @NotBlank
        String status
) {
}
