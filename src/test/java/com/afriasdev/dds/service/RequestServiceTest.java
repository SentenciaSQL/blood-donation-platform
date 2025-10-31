package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.request.CreateRequestDto;
import com.afriasdev.dds.api.dto.request.UpdateRequestStatusDto;
import com.afriasdev.dds.domain.Request;
import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.repository.RequestRepository;
import com.afriasdev.dds.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {

    @Mock
    RequestRepository requests;

    @Mock
    UserRepository users;

    @InjectMocks
    RequestService service;

    @Test
    void create_persists_with_defaults_and_fields() {
        var requester = new User();
        requester.setId(1L);

        var dto = new CreateRequestDto("O+", (short) 3, "Hospital A", null, null);

        when(requests.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = service.create(requester, dto);

        var captor = ArgumentCaptor.forClass(Request.class);
        verify(requests).save(captor.capture());
        var entity = captor.getValue();

        assertThat(entity.getRequester()).isEqualTo(requester);
        assertThat(entity.getBloodType()).isEqualTo("O+");
        assertThat(entity.getUrgency()).isEqualTo((short) 3);
        assertThat(entity.getStatus()).isEqualTo("OPEN");
        assertThat(saved).isSameAs(entity);
    }

    @Test
    void updateStatus_sets_status_and_optional_matchedDonor() {
        var req = Request.builder().id(99L).status("OPEN").build();
        when(requests.findById(99L)).thenReturn(Optional.of(req));
        var donor = new User();
        donor.setId(7L);
        when(users.findById(7L)).thenReturn(Optional.of(donor));

        var updated = service.updateStatus(99L, new UpdateRequestStatusDto("MATCHED", 7L));

        assertThat(updated.getStatus()).isEqualTo("MATCHED");
        assertThat(updated.getMatchedDonor().getId()).isEqualTo(7L);
        verifyNoMoreInteractions(users);
    }
}
