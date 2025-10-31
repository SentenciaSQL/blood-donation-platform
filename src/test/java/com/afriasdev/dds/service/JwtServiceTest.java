package com.afriasdev.dds.service;

import com.afriasdev.dds.security.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @Test
    void generate_and_parse_subject_ok() {
        var fixedNow = Instant.parse("2025-10-30T12:00:00Z");
        var clock = Clock.fixed(fixedNow, ZoneOffset.UTC);

        var svc = new JwtService();
        ReflectionTestUtils.setField(svc, "secret", "change-me-32chars-minimum-aaaaaaaaaaaa");
        ReflectionTestUtils.setField(svc, "expirationMinutes", 5L);

        var token = svc.generate("andres@test.com", java.util.Map.of("role", "ADMIN"));
        assertThat(token).isNotBlank();

        var subject = svc.getSubject(token);
        assertThat(subject).isEqualTo("andres@test.com");
    }

}
