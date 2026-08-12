package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.notification.CreateNotificationDto;
import com.afriasdev.dds.domain.Notification;
import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.exception.ForbiddenException;
import com.afriasdev.dds.repository.NotificationRepository;
import com.afriasdev.dds.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    NotificationRepository notes;

    @Mock
    UserRepository users;

    @InjectMocks
    NotificationService service;

    User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
    }

    @Test
    void create_shouldBuildAndSaveNotification() {
        var dto = new CreateNotificationDto(1L, "Title", "Body");
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(notes.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.create(dto);

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getTitle()).isEqualTo("Title");
        assertThat(result.getBody()).isEqualTo("Body");
        assertThat(result.getSeen()).isFalse();
        assertThat(result.getCreatedAt()).isNotNull();

        verify(users).findById(1L);
        verify(notes).save(any(Notification.class));
    }

    @Test
    void markSeen_shouldUpdateSeenFlag_forOwner() {
        var n = Notification.builder()
                .id(10L)
                .user(user)
                .title("T")
                .body("B")
                .seen(false)
                .build();

        when(notes.findById(10L)).thenReturn(Optional.of(n));

        var result = service.markSeen(10L, 1L, true);

        assertThat(result.getSeen()).isTrue();
        verify(notes).findById(10L);
        verifyNoMoreInteractions(notes);
    }

    @Test
    void markSeen_shouldReject_nonOwner() {
        var n = Notification.builder()
                .id(10L)
                .user(user)
                .title("T")
                .body("B")
                .seen(false)
                .build();

        when(notes.findById(10L)).thenReturn(Optional.of(n));

        assertThatThrownBy(() -> service.markSeen(10L, 99L, true))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void myNotifications_shouldReturnOnlyMatchingUser() {
        var other = new User();
        other.setId(2L);

        var n1 = Notification.builder().id(1L).user(user).title("A").build();
        var n2 = Notification.builder().id(2L).user(other).title("B").build();
        var n3 = Notification.builder().id(3L).user(user).title("C").build();

        when(notes.findAll()).thenReturn(List.of(n1, n2, n3));

        var list = service.myNotifications(1L);

        assertThat(list).hasSize(2);
        assertThat(list).extracting(Notification::getId).containsExactlyInAnyOrder(1L, 3L);
        verify(notes).findAll();
    }
}
