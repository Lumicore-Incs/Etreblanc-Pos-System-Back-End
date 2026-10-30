package com.selling.dto.get;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class OrderExportDto {
    private Integer customerId;
    private String name;
    private String address;
    private String contact01;
    private String contact02;
    private LocalDate date;
    private String waybillId;
    private BigDecimal price;
    private String status;
}