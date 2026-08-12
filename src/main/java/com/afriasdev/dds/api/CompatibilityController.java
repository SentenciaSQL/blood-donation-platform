package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.compatibility.CompatibilityResponseDto;
import com.afriasdev.dds.domain.BloodType;
import com.afriasdev.dds.exception.BadRequestException;
import com.afriasdev.dds.service.BloodCompatibilityService;
import com.afriasdev.dds.util.BloodTypeValidator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/compatibility")
public class CompatibilityController {

    private final BloodCompatibilityService compatibilityService;

    public CompatibilityController(BloodCompatibilityService compatibilityService) {
        this.compatibilityService = compatibilityService;
    }

    @GetMapping
    public CompatibilityResponseDto check(
            @RequestParam String donorType,
            @RequestParam String recipientType
    ) {
        if (!BloodTypeValidator.isValid(donorType) || !BloodTypeValidator.isValid(recipientType)) {
            throw new BadRequestException("Invalid blood type");
        }

        BloodType donor = BloodType.fromCode(donorType);
        BloodType recipient = BloodType.fromCode(recipientType);

        return new CompatibilityResponseDto(
                donor.getCode(),
                recipient.getCode(),
                compatibilityService.canDonateTo(donor, recipient),
                compatibilityService.getCompatibleDonorTypes(recipient).stream().map(BloodType::getCode).toList(),
                compatibilityService.getCompatibleRecipientTypes(donor).stream().map(BloodType::getCode).toList()
        );
    }
}
