package com.demo.api_gateway.DTO;

import lombok.Data;

@Data
public class LoginValidationResponseDTO {
    private boolean isMember;
    private Long userId;
    private String userName;
}

