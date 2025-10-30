package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.request.CreateRequestDto;
import com.afriasdev.dds.api.dto.request.UpdateRequestStatusDto;
import com.afriasdev.dds.domain.Request;
import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.repository.RequestRepository;
import com.afriasdev.dds.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class RequestService {
    private final RequestRepository requests;
    private final UserRepository users;

    public RequestService(RequestRepository requests, UserRepository users) {
        this.requests = requests;
        this.users = users;
    }

    @Transactional
    public Request create(User requester, CreateRequestDto dto) {
        Request req = Request.builder()
                .requester(requester)
                .bloodType(dto.bloodType())
                .urgency(dto.urgency() == null ? 1 : dto.urgency())
                .hospital(dto.hospital())
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .status("OPEN")
                .createdAt(Instant.now())
                .build();

        return requests.save(req);
    }

    @Transactional(readOnly = true)
    public List<Request> findAll() {
        return requests.findAll();
    }

    @Transactional(readOnly = true)
    public Request findById(Long id) {
        return requests.findById(id).orElseThrow();
    }

    @Transactional
    public Request updateStatus(Long id, UpdateRequestStatusDto dto) {
        Request req = findById(id);
        req.setStatus(dto.status());
        if (dto.matchedDonorUserId() != null) {
            req.setMatchedDonor(users.findById(dto.matchedDonorUserId()).orElseThrow());
        }
        return req;
    }

    @Transactional
    public void delete(Long id) {
        requests.deleteById(id);
    }
}
