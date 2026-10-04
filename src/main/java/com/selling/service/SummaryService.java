package com.selling.service;

import com.selling.dto.get.UserSummeryGet;

public interface SummaryService {

    UserSummeryGet getSummaryDetails(Integer id, Integer month);
    
}
