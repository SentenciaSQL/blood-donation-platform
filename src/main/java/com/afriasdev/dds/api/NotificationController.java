package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.notification.CreateNotificationDto;
import com.afriasdev.dds.api.dto.notification.MarkSeenDto;
import com.afriasdev.dds.domain.Notification;
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

    public NotificationController(NotificationService service, AuthUserService authUser) {
        this.service = service;
        this.authUser = authUser;
    }

    @GetMapping("/me")
    public List<Notification> myNotifications(Authentication auth) {
        var user = authUser.currentUser(auth);
        return service.myNotifications(user.getId());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Notification create(@Valid @RequestBody CreateNotificationDto dto) {
        return service.create(dto);
    }

    @PatchMapping("/{id}/seen")
    public Notification markSeen(@PathVariable Long id, @RequestBody MarkSeenDto dto, Authentication auth) {
        boolean seen = dto.seen() != null && dto.seen();
        return service.markSeen(id, seen);
    }
}
