package com.selling.service;

import com.selling.dto.UserDto;
import com.selling.dto.get.ExcelTypeDto;

import java.util.List;

public interface DashBoardService {
    List<ExcelTypeDto> findOrder(String name);

    List<ExcelTypeDto> ConformOrder();

    int getTotalOrder(UserDto user);

    int getTodayOrder(UserDto user);

    int getConformOrder(UserDto user);

    int getCancelOrder(UserDto user);

}
