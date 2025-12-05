package com.demo.api_gateway.DTO;

import lombok.Data;

@Data
public class LoginRequest {
    private String userName;
    private String password;
}
