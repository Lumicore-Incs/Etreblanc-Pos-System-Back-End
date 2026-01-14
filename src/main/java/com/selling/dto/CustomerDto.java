package com.selling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class CustomerDto {
    private Integer customerId;
    private String name;
    private String address;
    private String contact01;
    private String contact02;
    private LocalDateTime date;
    private String status; // active, inactive
    private Integer userId;
}
