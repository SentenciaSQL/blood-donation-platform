package com.afriasdev.dds.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

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
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Convert(converter = BloodTypeConverter.class)
    @Column(name = "blood_type", nullable = false, length = 3)
    private BloodType bloodType;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Column(length = 40)
    private String phone;

    @Column(length = 120)
    private String city;

    @Column(length = 240)
    private String address;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "last_donation_at")
    private Instant lastDonationDate;

    @Column(nullable = false)
    private Boolean eligible = true;

    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Legacy availability flag kept for backward compatibility with existing queries.
     */
    @Column(nullable = false)
    private Boolean availability = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
