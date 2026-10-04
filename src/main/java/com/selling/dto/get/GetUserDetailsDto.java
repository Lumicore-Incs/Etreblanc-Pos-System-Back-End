package com.selling.dto.get;

import com.selling.dto.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class GetUserDetailsDto {
    private List<OrderDto> order;
    private double income;
}
