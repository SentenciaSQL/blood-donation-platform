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
@Table(name = "inventories", uniqueConstraints = @UniqueConstraint(columnNames = {"blood_bank_id", "blood_type"}))
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_bank_id", nullable = false)
    private BloodBank bloodBank;

    @Convert(converter = BloodTypeConverter.class)
    @Column(name = "blood_type", nullable = false, length = 3)
    private BloodType bloodType;

    @Column(name = "units_available", nullable = false)
    private Integer unitsAvailable = 0;

    @Column(name = "minimum_stock", nullable = false)
    private Integer minimumStock = 5;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
