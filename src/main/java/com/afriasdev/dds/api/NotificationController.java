package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.notification.CreateNotificationDto;
import com.afriasdev.dds.api.dto.notification.MarkSeenDto;
import com.afriasdev.dds.api.dto.notification.NotificationResponseDto;
import com.afriasdev.dds.api.mapper.EntityMapper;
import com.afriasdev.dds.service.AuthUserService;
import com.afriasdev.dds.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService service;
    private final AuthUserService authUser;
    private final EntityMapper mapper;

    public NotificationController(NotificationService service, AuthUserService authUser, EntityMapper mapper) {
        this.service = service;
        this.authUser = authUser;
        this.mapper = mapper;
    }

    @GetMapping("/me")
    public List<NotificationResponseDto> myNotifications(Authentication auth) {
        var user = authUser.currentUser(auth);
        return service.myNotifications(user.getId()).stream()
                .map(mapper::toNotificationResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public NotificationResponseDto create(@Valid @RequestBody CreateNotificationDto dto) {
        return mapper.toNotificationResponse(service.create(dto));
    }

    @PatchMapping("/{id}/seen")
    public NotificationResponseDto markSeen(
            @PathVariable Long id,
            @Valid @RequestBody MarkSeenDto dto,
            Authentication auth
    ) {
        var user = authUser.currentUser(auth);
        boolean seen = dto.seen() != null && dto.seen();
        return mapper.toNotificationResponse(service.markSeen(id, user.getId(), seen));
    }
}
