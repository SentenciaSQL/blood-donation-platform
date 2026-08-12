package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.request.CreateRequestDto;
import com.afriasdev.dds.api.dto.request.UpdateRequestDto;
import com.afriasdev.dds.api.dto.request.UpdateRequestStatusDto;
import com.afriasdev.dds.domain.BloodType;
import com.afriasdev.dds.domain.Request;
import com.afriasdev.dds.domain.RequestStatus;
import com.afriasdev.dds.domain.RequestUrgency;
import com.afriasdev.dds.domain.Role;
import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.exception.BadRequestException;
import com.afriasdev.dds.exception.ForbiddenException;
import com.afriasdev.dds.exception.ResourceNotFoundException;
import com.afriasdev.dds.repository.RequestRepository;
import com.afriasdev.dds.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

@Service
public class RequestService {
    private static final Set<RequestStatus> UPDATABLE = EnumSet.of(
            RequestStatus.PENDING, RequestStatus.MATCHING, RequestStatus.MATCHED
    );
    private static final Set<RequestStatus> CANCELLABLE = EnumSet.of(
            RequestStatus.PENDING, RequestStatus.MATCHING, RequestStatus.MATCHED
    );

    private final RequestRepository requests;
    private final UserRepository users;
    private final NotificationService notifications;

    public RequestService(
            RequestRepository requests,
            UserRepository users,
            NotificationService notifications
    ) {
        this.requests = requests;
        this.users = users;
        this.notifications = notifications;
    }

    @Transactional
    public Request create(User requester, CreateRequestDto dto) {
        Request req = Request.builder()
                .requester(requester)
                .bloodType(BloodType.fromCode(dto.bloodType()))
                .unitsRequired(dto.unitsRequired())
                .hospitalName(dto.hospitalName())
                .patientName(dto.patientName())
                .contactPhone(dto.contactPhone())
                .city(dto.city())
                .description(dto.description())
                .urgency(dto.urgency() == null ? RequestUrgency.MEDIUM : dto.urgency())
                .status(RequestStatus.PENDING)
                .requiredDate(dto.requiredDate())
                .createdAt(Instant.now())
                .build();
        return requests.save(req);
    }

    @Transactional(readOnly = true)
    public Page<Request> findMine(User requester, Pageable pageable) {
        return requests.findByRequester(requester, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Request> search(
            BloodType bloodType,
            RequestUrgency urgency,
            RequestStatus status,
            String city,
            Instant fromDate,
            Instant toDate,
            Pageable pageable
    ) {
        return requests.search(bloodType, urgency, status, city, fromDate, toDate, pageable);
    }

    @Transactional(readOnly = true)
    public Request findById(Long id) {
        return requests.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found"));
    }

    @Transactional(readOnly = true)
    public Request findByIdForUser(Long id, User user) {
        Request req = findById(id);
        if (user.getRole() != Role.ADMIN && !req.getRequester().getId().equals(user.getId())) {
            throw new ForbiddenException("You cannot access another user's blood request");
        }
        return req;
    }

    @Transactional
    public Request update(Long id, User user, UpdateRequestDto dto) {
        Request req = findByIdForUser(id, user);
        if (!UPDATABLE.contains(req.getStatus())) {
            throw new BadRequestException("Only pending/matching/matched requests can be updated");
        }
        if (user.getRole() != Role.ADMIN && !req.getRequester().getId().equals(user.getId())) {
            throw new ForbiddenException("You cannot update another user's blood request");
        }

        if (dto.bloodType() != null) {
            req.setBloodType(BloodType.fromCode(dto.bloodType()));
        }
        if (dto.unitsRequired() != null) {
            req.setUnitsRequired(dto.unitsRequired());
        }
        if (dto.hospitalName() != null) {
            req.setHospitalName(dto.hospitalName());
        }
        if (dto.patientName() != null) {
            req.setPatientName(dto.patientName());
        }
        if (dto.contactPhone() != null) {
            req.setContactPhone(dto.contactPhone());
        }
        if (dto.city() != null) {
            req.setCity(dto.city());
        }
        if (dto.description() != null) {
            req.setDescription(dto.description());
        }
        if (dto.urgency() != null) {
            req.setUrgency(dto.urgency());
        }
        if (dto.requiredDate() != null) {
            req.setRequiredDate(dto.requiredDate());
        }
        req.setUpdatedAt(Instant.now());
        return req;
    }

    @Transactional
    public Request cancel(Long id, User user) {
        Request req = findByIdForUser(id, user);
        if (!CANCELLABLE.contains(req.getStatus())) {
            throw new BadRequestException("Request cannot be cancelled in status " + req.getStatus());
        }
        req.setStatus(RequestStatus.CANCELLED);
        req.setUpdatedAt(Instant.now());
        return req;
    }

    @Transactional
    public Request updateStatus(Long id, UpdateRequestStatusDto dto) {
        Request req = findById(id);
        RequestStatus previous = req.getStatus();
        req.setStatus(dto.status());
        if (dto.matchedDonorUserId() != null) {
            User matchedDonor = users.findById(dto.matchedDonorUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Matched donor user not found"));
            if (matchedDonor.getRole() != Role.DONOR) {
                throw new BadRequestException("Matched user must have DONOR role");
            }
            req.setMatchedDonor(matchedDonor);
        }
        req.setUpdatedAt(Instant.now());

        if (previous != dto.status()) {
            notifications.notifyRequestStatusChanged(req);
        }
        return req;
    }

    @Transactional
    public void delete(Long id) {
        if (!requests.existsById(id)) {
            throw new ResourceNotFoundException("Blood request not found");
        }
        requests.deleteById(id);
    }
}
