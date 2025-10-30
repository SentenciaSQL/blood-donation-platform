package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.donation.ScheduleDonationDto;
import com.afriasdev.dds.api.dto.donation.UpdateDonationStatusDto;
import com.afriasdev.dds.domain.Donation;
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

    public DonationController(DonationService service, AuthUserService authUser) {
        this.service = service;
        this.authUser = authUser;
    }

    @PreAuthorize("hasRole('DONOR')")
    @GetMapping("/me")
    public List<Donation> myDonations(Authentication auth) {
        var user = authUser.currentUser(auth);
        return service.myDonations(user);
    }

    @PreAuthorize("hasRole('DONOR')")
    @PostMapping("/schedule")
    public Donation schedule(@Valid @RequestBody ScheduleDonationDto dto, Authentication auth) {
        var user = authUser.currentUser(auth);
        return service.schedule(user, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public Donation updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateDonationStatusDto dto) {
        return service.updateStatus(id, dto);
    }

}
