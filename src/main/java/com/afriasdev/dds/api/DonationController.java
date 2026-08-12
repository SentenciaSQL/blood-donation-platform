package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.donation.DonationResponseDto;
import com.afriasdev.dds.api.dto.donation.ScheduleDonationDto;
import com.afriasdev.dds.api.dto.donation.UpdateDonationStatusDto;
import com.afriasdev.dds.api.mapper.EntityMapper;
import com.afriasdev.dds.service.AuthUserService;
import com.afriasdev.dds.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donations")
public class DonationController {

    private final DonationService service;
    private final AuthUserService authUser;
    private final EntityMapper mapper;

    public DonationController(DonationService service, AuthUserService authUser, EntityMapper mapper) {
        this.service = service;
        this.authUser = authUser;
        this.mapper = mapper;
    }

    @PreAuthorize("hasRole('DONOR')")
    @GetMapping("/me")
    public List<DonationResponseDto> myDonations(Authentication auth) {
        var user = authUser.currentUser(auth);
        return service.myDonations(user).stream()
                .map(mapper::toDonationResponse)
                .toList();
    }

    @PreAuthorize("hasRole('DONOR')")
    @PostMapping("/schedule")
    public DonationResponseDto schedule(
            @Valid @RequestBody ScheduleDonationDto dto,
            Authentication auth
    ) {
        var user = authUser.currentUser(auth);
        return mapper.toDonationResponse(service.schedule(user, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public DonationResponseDto updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDonationStatusDto dto
    ) {
        return mapper.toDonationResponse(service.updateStatus(id, dto));
    }
}
