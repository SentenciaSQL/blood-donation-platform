package com.afriasdev.dds.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "requests")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "requester_user_id", nullable = false)
    private User requester;

    @Column(name = "blood_type", nullable = false, length = 3)
    private String bloodType;

    private  Short urgency = 1;
    private String hospital;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Column(nullable = false, length = 16)
    private String status = "OPEN";

    @ManyToOne
    @JoinColumn(name = "matched_donor_user_id")
    private User matchedDonor;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
