package com.afriasdev.dds.repository;

import com.afriasdev.dds.domain.BloodBank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BloodBankRepository extends JpaRepository<BloodBank, Long> {

    @Query("""
            SELECT b FROM BloodBank b
            WHERE (:city IS NULL OR LOWER(b.city) = LOWER(:city))
              AND (:active IS NULL OR b.active = :active)
            """)
    Page<BloodBank> search(
            @Param("city") String city,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
