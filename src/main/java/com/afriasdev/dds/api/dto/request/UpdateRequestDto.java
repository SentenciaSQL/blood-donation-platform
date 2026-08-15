package com.afriasdev.dds.api.dto.request;

import com.afriasdev.dds.domain.RequestUrgency;
import com.afriasdev.dds.validation.ValidBloodType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateRequestDto(
        @ValidBloodType String bloodType,
        @Min(1) Integer unitsRequired,
        @Size(max = 200) String hospitalName,
        @Size(max = 160) String patientName,
        @Size(max = 40) String contactPhone,
        @Size(max = 120) String city,
        String description,
        RequestUrgency urgency,
        @FutureOrPresent LocalDate requiredDate
) {
}
