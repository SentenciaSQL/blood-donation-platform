package com.afriasdev.dds.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "donor_user_id", nullable = false)
    private User donor;

    @ManyToOne
    @JoinColumn(name = "blood_bank_id", nullable = false)
    private BloodBank bloodBank;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(nullable = false, length = 16)
    private String status = "SCHEDULED";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
