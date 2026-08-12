package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.request.CreateRequestDto;
import com.afriasdev.dds.api.dto.request.UpdateRequestStatusDto;
import com.afriasdev.dds.domain.BloodType;
import com.afriasdev.dds.domain.Request;
import com.afriasdev.dds.domain.RequestStatus;
import com.afriasdev.dds.domain.RequestUrgency;
import com.afriasdev.dds.domain.Role;
import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.repository.RequestRepository;
import com.afriasdev.dds.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock RequestRepository requests;
    @Mock UserRepository users;
    @Mock NotificationService notifications;

    @InjectMocks RequestService service;

    @Test
    void create_persists_with_defaults_and_fields() {
        var requester = new User();
        requester.setId(1L);
        requester.setRole(Role.REQUESTER);

        var dto = new CreateRequestDto(
                "O+",
                2,
                "Hospital A",
                "Patient",
                "8095550000",
                "Santo Domingo",
                "Urgent need",
                RequestUrgency.CRITICAL,
                LocalDate.now().plusDays(2)
        );

        when(requests.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = service.create(requester, dto);

        var captor = ArgumentCaptor.forClass(Request.class);
        verify(requests).save(captor.capture());
        var entity = captor.getValue();

        assertThat(entity.getRequester()).isEqualTo(requester);
        assertThat(entity.getBloodType()).isEqualTo(BloodType.O_POS);
        assertThat(entity.getUrgency()).isEqualTo(RequestUrgency.CRITICAL);
        assertThat(entity.getStatus()).isEqualTo(RequestStatus.PENDING);
        assertThat(entity.getUnitsRequired()).isEqualTo(2);
        assertThat(saved).isSameAs(entity);
    }

    @Test
    void updateStatus_sets_status_and_optional_matchedDonor() {
        var req = Request.builder().id(99L).status(RequestStatus.PENDING).build();
        when(requests.findById(99L)).thenReturn(Optional.of(req));
        var donor = new User();
        donor.setId(7L);
        donor.setRole(Role.DONOR);
        when(users.findById(7L)).thenReturn(Optional.of(donor));

        var updated = service.updateStatus(99L, new UpdateRequestStatusDto(RequestStatus.MATCHED, 7L));

        assertThat(updated.getStatus()).isEqualTo(RequestStatus.MATCHED);
        assertThat(updated.getMatchedDonor().getId()).isEqualTo(7L);
        verify(notifications).notifyRequestStatusChanged(req);
    }
}
