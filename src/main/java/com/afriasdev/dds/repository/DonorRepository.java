package com.afriasdev.dds.repository;

import com.afriasdev.dds.domain.BloodType;
import com.afriasdev.dds.domain.Donor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DonorRepository extends JpaRepository<Donor, Long> {

    List<Donor> findByBloodTypeAndAvailabilityTrue(BloodType bloodType);

    Optional<Donor> findByUserId(Long userId);

    @Query("""
            SELECT d FROM Donor d
            WHERE (:bloodType IS NULL OR d.bloodType = :bloodType)
              AND (:city IS NULL OR LOWER(d.city) = LOWER(:city))
              AND (:eligible IS NULL OR d.eligible = :eligible)
              AND (:active IS NULL OR d.active = :active)
            """)
    Page<Donor> search(
            @Param("bloodType") BloodType bloodType,
            @Param("city") String city,
            @Param("eligible") Boolean eligible,
            @Param("active") Boolean active,
            Pageable pageable
    );

    @Query("""
            SELECT d FROM Donor d
            WHERE d.active = true
              AND d.eligible = true
              AND d.bloodType IN :bloodTypes
            """)
    List<Donor> findActiveEligibleByBloodTypes(@Param("bloodTypes") Collection<BloodType> bloodTypes);
}
