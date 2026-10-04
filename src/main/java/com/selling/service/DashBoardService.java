package com.selling.service;

import com.selling.dto.UserDto;
import com.selling.dto.get.ExcelTypeDto;
import com.selling.dto.get.WeeklyUserOrderDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DashBoardService {
    List<ExcelTypeDto> findOrder(String name);

    String ConformOrder(List<String> serialNumbers);

    int getTotalOrder(UserDto user);

    int getTodayOrder(UserDto user);

    int getConformOrder(UserDto user);

    int getConformOrderByUser(UserDto user);

    int getCancelOrder(UserDto user);

    int getCancelOrderByUser(UserDto user);

    List<Object> findOrderQty();

    int processingOrders(UserDto userDto);

    Map<String, Integer> DailyUsersOrders(LocalDate date);

    List<WeeklyUserOrderDto> findWeeklyOrder();

}
