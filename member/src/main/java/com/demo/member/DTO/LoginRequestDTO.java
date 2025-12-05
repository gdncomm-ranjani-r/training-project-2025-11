package com.demo.member.DTO;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String userName;
    private String password;
}
