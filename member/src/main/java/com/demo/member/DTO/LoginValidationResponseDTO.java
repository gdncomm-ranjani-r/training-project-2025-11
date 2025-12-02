package com.demo.member.DTO;


import lombok.Builder;
import lombok.Data;

@Data
public class LoginValidationResponseDTO {
    private boolean isMember;
    private Long userId;
    private String email;
}
