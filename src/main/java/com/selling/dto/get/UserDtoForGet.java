package com.selling.dto.get;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.selling.dto.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class UserDtoForGet {
    private Long id;
    private String name;
    private String email;
    private String telephone;
    private String role;
    private String registration_date;
    private String status;
    private String type;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private ProductDto productId;
}