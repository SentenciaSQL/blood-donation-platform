package com.afriasdev.dds.repository;

import com.afriasdev.dds.domain.BloodType;
import com.afriasdev.dds.domain.Request;
import com.afriasdev.dds.domain.RequestStatus;
import com.afriasdev.dds.domain.RequestUrgency;
import com.afriasdev.dds.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByRequesterOrderByCreatedAtDesc(User requester);

    Page<Request> findByRequester(User requester, Pageable pageable);

    @Query("""
            SELECT r FROM Request r
            WHERE (:bloodType IS NULL OR r.bloodType = :bloodType)
              AND (:urgency IS NULL OR r.urgency = :urgency)
              AND (:status IS NULL OR r.status = :status)
              AND (:city IS NULL OR LOWER(r.city) = LOWER(:city))
              AND (:fromDate IS NULL OR r.createdAt >= :fromDate)
              AND (:toDate IS NULL OR r.createdAt <= :toDate)
            """)
    Page<Request> search(
            @Param("bloodType") BloodType bloodType,
            @Param("urgency") RequestUrgency urgency,
            @Param("status") RequestStatus status,
            @Param("city") String city,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable
    );
}
