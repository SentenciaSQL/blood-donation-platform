package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.common.PageResponse;
import com.afriasdev.dds.api.dto.request.CreateRequestDto;
import com.afriasdev.dds.api.dto.request.RequestResponseDto;
import com.afriasdev.dds.api.dto.request.UpdateRequestDto;
import com.afriasdev.dds.api.dto.request.UpdateRequestStatusDto;
import com.afriasdev.dds.api.mapper.EntityMapper;
import com.afriasdev.dds.domain.BloodType;
import com.afriasdev.dds.domain.RequestStatus;
import com.afriasdev.dds.domain.RequestUrgency;
import com.afriasdev.dds.service.AuthUserService;
import com.afriasdev.dds.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private final RequestService service;
    private final AuthUserService authUser;
    private final EntityMapper mapper;

    public RequestController(RequestService service, AuthUserService authUser, EntityMapper mapper) {
        this.service = service;
        this.authUser = authUser;
        this.mapper = mapper;
    }

    @PreAuthorize("hasAnyRole('REQUESTER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<RequestResponseDto> create(
            @Valid @RequestBody CreateRequestDto dto,
            Authentication auth
    ) {
        var user = authUser.currentUser(auth);
        var saved = service.create(user, dto);
        return ResponseEntity
                .created(URI.create("/requests/" + saved.getId()))
                .body(mapper.toRequestResponse(saved));
    }

    @PreAuthorize("hasAnyRole('REQUESTER', 'ADMIN')")
    @GetMapping("/me")
    public PageResponse<RequestResponseDto> myRequests(
            Authentication auth,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        var user = authUser.currentUser(auth);
        return PageResponse.from(service.findMine(user, pageable).map(mapper::toRequestResponse));
    }

    @PreAuthorize("hasAnyRole('REQUESTER', 'ADMIN')")
    @GetMapping("/{id}")
    public RequestResponseDto get(@PathVariable Long id, Authentication auth) {
        return mapper.toRequestResponse(service.findByIdForUser(id, authUser.currentUser(auth)));
    }

    @PreAuthorize("hasAnyRole('REQUESTER', 'ADMIN')")
    @PutMapping("/{id}")
    public RequestResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequestDto dto,
            Authentication auth
    ) {
        return mapper.toRequestResponse(service.update(id, authUser.currentUser(auth), dto));
    }

    @PreAuthorize("hasAnyRole('REQUESTER', 'ADMIN')")
    @PatchMapping("/{id}/cancel")
    public RequestResponseDto cancel(@PathVariable Long id, Authentication auth) {
        return mapper.toRequestResponse(service.cancel(id, authUser.currentUser(auth)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public PageResponse<RequestResponseDto> search(
            @RequestParam(required = false) String bloodType,
            @RequestParam(required = false) RequestUrgency urgency,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        BloodType type = bloodType == null || bloodType.isBlank() ? null : BloodType.fromCode(bloodType);
        return PageResponse.from(
                service.search(type, urgency, status, city, fromDate, toDate, pageable)
                        .map(mapper::toRequestResponse)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public RequestResponseDto updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequestStatusDto dto
    ) {
        return mapper.toRequestResponse(service.updateStatus(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
