package com.afriasdev.dds.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateRequestStatusDto(
        @NotBlank
        String status,
        Long matchedDonorUserId
) { }
