package com.afriasdev.dds.repository;

import com.afriasdev.dds.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
