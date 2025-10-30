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
@Table(name = "donors")
public class Donor {

    @Id
    private Long userId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name="blood_type", nullable=false, length=3)
    private String bloodType;

    private Instant lastDonationAt;

    @Column(nullable=false)
    private Boolean availability = true;

    private BigDecimal latitude;
    private BigDecimal longitude;
}
