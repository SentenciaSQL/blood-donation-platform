package com.afriasdev.dds.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

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

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_user_id", nullable = false)
    private User requester;

    @Convert(converter = BloodTypeConverter.class)
    @Column(name = "blood_type", nullable = false, length = 3)
    private BloodType bloodType;

    @Column(name = "units_required", nullable = false)
    private Integer unitsRequired = 1;

    @Column(name = "hospital_name", length = 200)
    private String hospitalName;

    @Column(name = "patient_name", length = 160)
    private String patientName;

    @Column(name = "contact_phone", length = 40)
    private String contactPhone;

    @Column(length = 120)
    private String city;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestUrgency urgency = RequestUrgency.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "required_date")
    private LocalDate requiredDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_donor_user_id")
    private User matchedDonor;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
