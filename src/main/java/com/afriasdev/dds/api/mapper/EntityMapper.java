package com.afriasdev.dds.api.mapper;

import com.afriasdev.dds.api.dto.bank.BloodBankResponseDto;
import com.afriasdev.dds.api.dto.donation.DonationResponseDto;
import com.afriasdev.dds.api.dto.donor.DonorResponseDto;
import com.afriasdev.dds.api.dto.inventory.InventoryResponseDto;
import com.afriasdev.dds.api.dto.notification.NotificationResponseDto;
import com.afriasdev.dds.api.dto.request.RequestResponseDto;
import com.afriasdev.dds.api.dto.user.UserSummaryDto;
import com.afriasdev.dds.domain.BloodBank;
import com.afriasdev.dds.domain.Donation;
import com.afriasdev.dds.domain.Donor;
import com.afriasdev.dds.domain.Inventory;
import com.afriasdev.dds.domain.Notification;
import com.afriasdev.dds.domain.Request;
import com.afriasdev.dds.domain.User;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    public UserSummaryDto toUserSummary(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getActive(),
                user.getCreatedAt()
        );
    }

    public DonorResponseDto toDonorResponse(Donor donor) {
        if (donor == null) {
            return null;
        }
        return new DonorResponseDto(
                donor.getUserId(),
                toUserSummary(donor.getUser()),
                donor.getBloodType() == null ? null : donor.getBloodType().getCode(),
                donor.getBirthDate(),
                donor.getGender(),
                donor.getPhone(),
                donor.getCity(),
                donor.getAddress(),
                donor.getLatitude(),
                donor.getLongitude(),
                donor.getWeight(),
                donor.getLastDonationDate(),
                donor.getEligible(),
                donor.getActive(),
                donor.getCreatedAt(),
                donor.getUpdatedAt()
        );
    }

    public BloodBankResponseDto toBloodBankResponse(BloodBank bank) {
        if (bank == null) {
            return null;
        }
        return new BloodBankResponseDto(
                bank.getId(),
                bank.getName(),
                bank.getAddress(),
                bank.getCity(),
                bank.getPhone(),
                bank.getEmail(),
                bank.getLatitude(),
                bank.getLongitude(),
                bank.getActive(),
                bank.getCreatedAt(),
                bank.getUpdatedAt()
        );
    }

    public InventoryResponseDto toInventoryResponse(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        Long bankId = inventory.getBloodBank() == null ? null : inventory.getBloodBank().getId();
        String bankName = inventory.getBloodBank() == null ? null : inventory.getBloodBank().getName();
        int units = inventory.getUnitsAvailable() == null ? 0 : inventory.getUnitsAvailable();
        int min = inventory.getMinimumStock() == null ? 0 : inventory.getMinimumStock();
        return new InventoryResponseDto(
                inventory.getId(),
                bankId,
                bankName,
                inventory.getBloodType() == null ? null : inventory.getBloodType().getCode(),
                units,
                min,
                units <= min,
                inventory.getUpdatedAt()
        );
    }

    public RequestResponseDto toRequestResponse(Request request) {
        if (request == null) {
            return null;
        }
        return new RequestResponseDto(
                request.getId(),
                toUserSummary(request.getRequester()),
                request.getBloodType() == null ? null : request.getBloodType().getCode(),
                request.getUnitsRequired(),
                request.getHospitalName(),
                request.getPatientName(),
                request.getContactPhone(),
                request.getCity(),
                request.getDescription(),
                request.getUrgency(),
                request.getStatus(),
                request.getRequiredDate(),
                toUserSummary(request.getMatchedDonor()),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }

    public DonationResponseDto toDonationResponse(Donation donation) {
        if (donation == null) {
            return null;
        }
        Long requestId = donation.getRequest() == null ? null : donation.getRequest().getId();
        return new DonationResponseDto(
                donation.getId(),
                toUserSummary(donation.getDonor()),
                toBloodBankResponse(donation.getBloodBank()),
                requestId,
                donation.getAppointmentDate(),
                donation.getStatus(),
                donation.getBloodType() == null ? null : donation.getBloodType().getCode(),
                donation.getUnitsCollected(),
                donation.getNotes(),
                donation.getCreatedAt(),
                donation.getUpdatedAt()
        );
    }

    public NotificationResponseDto toNotificationResponse(Notification notification) {
        if (notification == null) {
            return null;
        }
        Long userId = notification.getUser() == null ? null : notification.getUser().getId();
        return new NotificationResponseDto(
                notification.getId(),
                userId,
                notification.getTitle(),
                notification.getBody(),
                notification.getSeen(),
                notification.getCreatedAt()
        );
    }
}
