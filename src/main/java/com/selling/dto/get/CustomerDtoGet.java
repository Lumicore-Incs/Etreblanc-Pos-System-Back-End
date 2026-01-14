package com.selling.dto.get;

import java.time.LocalDateTime;

import com.selling.dto.UserDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class CustomerDtoGet {
    private Integer customerId;
    private String name;
    private String address;
    private String contact01;
    private String contact02;
    private LocalDateTime date;
    private String status; // active, inactive
    private UserDto user;
}
