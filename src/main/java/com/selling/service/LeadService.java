package com.selling.service;

import java.util.List;

import com.selling.dto.LeadRequestDto;
import com.selling.dto.get.LeadDtoGet;
import com.selling.dto.get.LeadQtyDtoGet;

public interface LeadService {
    List<LeadDtoGet> saveLead(List<LeadRequestDto> requests, Long requesterId, boolean administrator);

    List<LeadQtyDtoGet> getLeadQty();

    List<LeadDtoGet> getLeadsByUserId(Long userId);

    LeadDtoGet updateLeadStatus(Integer leadId, String status, Long requesterId, boolean administrator);
}
