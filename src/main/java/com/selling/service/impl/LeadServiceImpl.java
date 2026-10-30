package com.selling.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.selling.dto.LeadRequestDto;
import com.selling.dto.get.LeadDtoGet;
import com.selling.dto.get.LeadQtyDtoGet;
import com.selling.model.Lead;
import com.selling.model.User;
import com.selling.repository.LeadRepo;
import com.selling.repository.UserRepo;
import com.selling.service.LeadService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {
    private final LeadRepo leadRepo;
    private final UserRepo userRepo;

    @Override
    @Transactional
    public List<LeadDtoGet> saveLead(
            List<LeadRequestDto> requests,
            Long requesterId,
            boolean administrator) {

        if (requests == null || requests.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one lead is required"
            );
        }

        List<Lead> leadsToSave = new ArrayList<>();

        // Same request එකේ duplicate contacts check කිරීම
        Set<String> processedContacts = new HashSet<>();

        for (LeadRequestDto request : requests) {

            if (request == null) {
                continue;
            }

            LocalDateTime leadDate = request.getDate() == null
                    ? LocalDateTime.now()
                    : request.getDate();

            LocalDate leadDay = leadDate.toLocalDate();

            LocalDateTime startDate = leadDay.atStartOfDay();
            LocalDateTime endDate = leadDay.plusDays(1).atStartOfDay();

            Long userId = administrator && request.getUserId() != null
                    ? request.getUserId()
                    : requesterId;

            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "User not found with id: " + userId
                    ));

            String contact01 = normalizeContact(request.getContact01());
            String contact02 = normalizeContact(request.getContact02());

            Set<String> contacts = new HashSet<>();

            if (!contact01.isEmpty()) {
                contacts.add(contact01);
            }

            if (!contact02.isEmpty()) {
                contacts.add(contact02);
            }

            boolean duplicate = false;

            for (String contact : contacts) {

                // Database එකේ එම දිනයේ number එක තිබෙනවාද?
                if (leadRepo.existsByContactAndDate(
                        contact, startDate, endDate)) {
                    duplicate = true;
                    break;
                }

                // මේ request එක තුළම number එක කලින් භාවිතා කරලාද?
                String key = leadDay + "|" + contact;

                if (processedContacts.contains(key)) {
                    duplicate = true;
                    break;
                }
            }

            if (duplicate) {
                // Duplicate lead එක skip කරනවා.
                continue;
            }

            // මේ lead එකේ contacts reserve කරනවා
            for (String contact : contacts) {
                processedContacts.add(leadDay + "|" + contact);
            }

            Lead lead = new Lead();

            lead.setDate(leadDate);
            lead.setName(request.getName());
            lead.setContact01(request.getContact01());
            lead.setContact02(request.getContact02());
            lead.setAddress(request.getAddress());
            lead.setType(request.getType());

            lead.setStatus(
                    request.getStatus() == null
                            || request.getStatus().isBlank()
                            ? "NEW"
                            : request.getStatus()
            );

            lead.setUser(user);

            leadsToSave.add(lead);
        }

        // Duplicate නොවන leads පමණක් save කරනවා
        if (leadsToSave.isEmpty()) {
            return new ArrayList<>();
        }

        return leadRepo.saveAll(leadsToSave)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadQtyDtoGet> getLeadQty() {
        return leadRepo.findLeadQtyByUser();
    }



    @Override
    @Transactional(readOnly = true)
    public List<LeadDtoGet> getLeadsByUserId(Long userId) {

        LocalDate today = LocalDate.now();

        LocalDateTime startDate = today.minusDays(2).atStartOfDay();
        LocalDateTime endDate = today.plusDays(1).atStartOfDay();

        return leadRepo
                .findAllByUserIdAndDateGreaterThanEqualAndDateLessThanOrderByDateDesc(
                        userId,
                        startDate,
                        endDate
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public LeadDtoGet updateLeadStatus(Integer leadId, String status, Long requesterId, boolean administrator) {
        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        }

        Lead lead = (administrator
                ? leadRepo.findById(leadId)
                : leadRepo.findByIdAndUserId(leadId, requesterId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found"));
        lead.setStatus(status);
        return toDto(leadRepo.save(lead));
    }

    private LeadDtoGet toDto(Lead lead) {
        return new LeadDtoGet(
                lead.getId(),
                lead.getDate(),
                lead.getName(),
                lead.getContact01(),
                lead.getContact02(),
                lead.getAddress(),
                lead.getType(),
                lead.getStatus(),
                lead.getUser().getId());
    }

    private String normalizeContact(String contact) {
        return contact == null ? "" : contact.trim();
    }
}

