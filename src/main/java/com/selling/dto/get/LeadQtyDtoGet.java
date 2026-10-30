package com.selling.dto.get;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeadQtyDtoGet {
    private Long userId;
    private String userName;
    private Long qty;
}
