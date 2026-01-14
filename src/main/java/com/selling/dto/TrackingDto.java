package com.selling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class TrackingDto {
    private String wayBillNo;
    private String orderId;
    private String customerName;
    private String contact;
}
