package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.request.CreateRequestDto;
import com.afriasdev.dds.api.dto.request.RequestResponseDto;
import com.afriasdev.dds.api.dto.request.UpdateRequestStatusDto;
import com.afriasdev.dds.api.mapper.EntityMapper;
import com.afriasdev.dds.service.AuthUserService;
import com.afriasdev.dds.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<RequestResponseDto> all() {
        return service.findAll().stream()
                .map(mapper::toRequestResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public RequestResponseDto get(@PathVariable Long id) {
        return mapper.toRequestResponse(service.findById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
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
