package com.selling.dto.get;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class ExcelTypeDto {
    private int id;
    private String name;
    private String address;
    private String contact01;
    private String contact02;
    private String price;
}
