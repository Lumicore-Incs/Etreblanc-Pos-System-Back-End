package com.selling.dto.get;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeadDtoGet {
    private Integer id;
    private LocalDateTime date;
    private String name;
    private String contact01;
    private String contact02;
    private String address;
    private String type;
    private String status;
    private Long userId;
}

