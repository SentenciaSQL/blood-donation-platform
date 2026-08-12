package com.afriasdev.dds.api.dto.donor;

import com.afriasdev.dds.domain.Gender;
import com.afriasdev.dds.validation.ValidBloodType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateDonorProfileDto(
        @ValidBloodType String bloodType,
        @Past LocalDate birthDate,
        Gender gender,
        @Size(max = 40) String phone,
        @Size(max = 120) String city,
        @Size(max = 240) String address,
        BigDecimal latitude,
        BigDecimal longitude,
        @DecimalMin("0.0") BigDecimal weight,
        Boolean active
) {
}
