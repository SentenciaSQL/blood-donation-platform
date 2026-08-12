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
                donor.getBloodType(),
                donor.getLastDonationAt(),
                donor.getAvailability()
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
                bank.getPhone(),
                bank.getLatitude(),
                bank.getLongitude(),
                bank.getCreatedAt()
        );
    }

    public InventoryResponseDto toInventoryResponse(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        Long bankId = inventory.getBloodBank() == null ? null : inventory.getBloodBank().getId();
        return new InventoryResponseDto(
                inventory.getId(),
                bankId,
                inventory.getBloodType(),
                inventory.getUnitsAvailable(),
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
                request.getBloodType(),
                request.getUrgency(),
                request.getHospital(),
                request.getLatitude(),
                request.getLongitude(),
                request.getStatus(),
                toUserSummary(request.getMatchedDonor()),
                request.getCreatedAt()
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
                donation.getScheduledAt(),
                donation.getStatus(),
                donation.getCreatedAt()
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
