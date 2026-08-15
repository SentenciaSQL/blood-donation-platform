package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.common.PageResponse;
import com.afriasdev.dds.api.dto.donation.DonationResponseDto;
import com.afriasdev.dds.api.dto.donor.DonorEligibilityDto;
import com.afriasdev.dds.api.dto.donor.DonorResponseDto;
import com.afriasdev.dds.api.dto.donor.UpdateDonorProfileDto;
import com.afriasdev.dds.api.mapper.EntityMapper;
import com.afriasdev.dds.domain.BloodType;
import com.afriasdev.dds.service.AuthUserService;
import com.afriasdev.dds.service.DonorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donors")
public class DonorController {

    private final DonorService donorService;
    private final AuthUserService authUserService;
    private final EntityMapper mapper;

    public DonorController(
            DonorService donorService,
            AuthUserService authUserService,
            EntityMapper mapper
    ) {
        this.donorService = donorService;
        this.authUserService = authUserService;
        this.mapper = mapper;
    }

    @PreAuthorize("hasRole('DONOR')")
    @GetMapping("/me")
    public DonorResponseDto myProfile(Authentication auth) {
        return mapper.toDonorResponse(donorService.getMyProfile(authUserService.currentUser(auth)));
    }

    @PreAuthorize("hasRole('DONOR')")
    @PutMapping("/me")
    public DonorResponseDto updateMyProfile(
            @Valid @RequestBody UpdateDonorProfileDto dto,
            Authentication auth
    ) {
        return mapper.toDonorResponse(donorService.updateMyProfile(authUserService.currentUser(auth), dto));
    }

    @PreAuthorize("hasRole('DONOR')")
    @GetMapping("/me/eligibility")
    public DonorEligibilityDto myEligibility(Authentication auth) {
        return donorService.getEligibility(authUserService.currentUser(auth));
    }

    @PreAuthorize("hasRole('DONOR')")
    @GetMapping("/me/history")
    public List<DonationResponseDto> myHistory(Authentication auth) {
        return donorService.getHistory(authUserService.currentUser(auth)).stream()
                .map(mapper::toDonationResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public PageResponse<DonorResponseDto> search(
            @RequestParam(required = false) String bloodType,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean eligible,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "userId", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        BloodType type = bloodType == null || bloodType.isBlank() ? null : BloodType.fromCode(bloodType);
        return PageResponse.from(
                donorService.search(type, city, eligible, active, pageable).map(mapper::toDonorResponse)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public DonorResponseDto getById(@PathVariable Long id) {
        return mapper.toDonorResponse(donorService.getById(id));
    }
}
