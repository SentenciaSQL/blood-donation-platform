package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.notification.CreateNotificationDto;
import com.afriasdev.dds.domain.Inventory;
import com.afriasdev.dds.domain.Donation;
import com.afriasdev.dds.domain.Notification;
import com.afriasdev.dds.domain.Request;
import com.afriasdev.dds.domain.Role;
import com.afriasdev.dds.domain.User;
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
        return createForUser(user, dto.title(), dto.body());
    }

    @Transactional
    public Notification createForUser(User user, String title, String body) {
        var n = Notification.builder()
                .user(user)
                .title(title)
                .body(body)
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

    @Transactional
    public void notifyDonationConfirmed(Donation donation) {
        createForUser(
                donation.getDonor(),
                "Donation confirmed",
                "Your donation appointment on " + donation.getAppointmentDate() + " has been confirmed."
        );
    }

    @Transactional
    public void notifyDonationCompleted(Donation donation) {
        createForUser(
                donation.getDonor(),
                "Donation completed",
                "Thank you! Your donation was completed successfully."
        );
    }

    @Transactional
    public void notifyDonationCancelled(Donation donation) {
        createForUser(
                donation.getDonor(),
                "Donation cancelled",
                "Your donation appointment on " + donation.getAppointmentDate() + " was cancelled."
        );
    }

    @Transactional
    public void notifyRequestStatusChanged(Request request) {
        createForUser(
                request.getRequester(),
                "Blood request update",
                "Your blood request status is now " + request.getStatus() + "."
        );
    }

    @Transactional
    public void notifyAdminsLowStock(Inventory inventory) {
        String bloodType = inventory.getBloodType() == null ? "?" : inventory.getBloodType().getCode();
        String bankName = inventory.getBloodBank() == null ? "unknown bank" : inventory.getBloodBank().getName();
        String title = "Low blood stock";
        String body = "Stock for " + bloodType + " at " + bankName
                + " is low (" + inventory.getUnitsAvailable() + " units).";

        users.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN && Boolean.TRUE.equals(u.getActive()))
                .forEach(admin -> createForUser(admin, title, body));
    }
}
