package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.common.PageResponse;
import com.afriasdev.dds.api.dto.donation.DonationResponseDto;
import com.afriasdev.dds.api.dto.donation.ScheduleDonationDto;
import com.afriasdev.dds.api.mapper.EntityMapper;
import com.afriasdev.dds.service.AuthUserService;
import com.afriasdev.dds.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
        return service.myDonations(authUser.currentUser(auth)).stream()
                .map(mapper::toDonationResponse)
                .toList();
    }

    @PreAuthorize("hasRole('DONOR')")
    @PostMapping("/schedule")
    public DonationResponseDto schedule(
            @Valid @RequestBody ScheduleDonationDto dto,
            Authentication auth
    ) {
        return mapper.toDonationResponse(service.schedule(authUser.currentUser(auth), dto));
    }

    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    @GetMapping("/{id}")
    public DonationResponseDto get(@PathVariable Long id, Authentication auth) {
        return mapper.toDonationResponse(service.findByIdForUser(id, authUser.currentUser(auth)));
    }

    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    @PatchMapping("/{id}/cancel")
    public DonationResponseDto cancel(@PathVariable Long id, Authentication auth) {
        return mapper.toDonationResponse(service.cancel(id, authUser.currentUser(auth)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public PageResponse<DonationResponseDto> all(
            @PageableDefault(size = 20, sort = "appointmentDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return PageResponse.from(service.findAll(pageable).map(mapper::toDonationResponse));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/confirm")
    public DonationResponseDto confirm(@PathVariable Long id) {
        return mapper.toDonationResponse(service.confirm(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/complete")
    public DonationResponseDto complete(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Integer> body
    ) {
        Integer units = body == null ? null : body.get("unitsCollected");
        return mapper.toDonationResponse(service.complete(id, units));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/no-show")
    public DonationResponseDto noShow(@PathVariable Long id) {
        return mapper.toDonationResponse(service.markNoShow(id));
    }
}
