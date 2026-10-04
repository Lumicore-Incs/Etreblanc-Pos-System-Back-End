package com.selling.dto.get;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WeeklyUserOrderDto {

    private String day;
    private Map<String, Integer> users;
}