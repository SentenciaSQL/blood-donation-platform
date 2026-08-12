package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.notification.CreateNotificationDto;
import com.afriasdev.dds.domain.Notification;
import com.afriasdev.dds.exception.ForbiddenException;
import com.afriasdev.dds.exception.ResourceNotFoundException;
import com.afriasdev.dds.repository.NotificationRepository;
import com.afriasdev.dds.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notes;
    private final UserRepository users;

    public NotificationService(NotificationRepository notes, UserRepository users) {
        this.notes = notes;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<Notification> myNotifications(Long userId) {
        return notes.findAll().stream()
                .filter(n -> n.getUser() != null && n.getUser().getId().equals(userId))
                .toList();
    }

    @Transactional
    public Notification create(CreateNotificationDto dto) {
        var user = users.findById(dto.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        var n = Notification.builder()
                .user(user)
                .title(dto.title())
                .body(dto.body())
                .seen(false)
                .createdAt(Instant.now())
                .build();
        return notes.save(n);
    }

    @Transactional
    public Notification markSeen(Long id, Long currentUserId, boolean seen) {
        var n = notes.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (n.getUser() == null || !n.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("You cannot modify another user's notification");
        }

        n.setSeen(seen);
        return n;
    }
}
