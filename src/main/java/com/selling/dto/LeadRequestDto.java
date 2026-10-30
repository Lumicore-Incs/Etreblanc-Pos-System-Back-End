package com.selling.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeadRequestDto {
    private LocalDateTime date;
    private String name;
    private String contact01;
    private String contact02;
    private String address;
    private String type;
    private String status;
    private Long userId;
}
