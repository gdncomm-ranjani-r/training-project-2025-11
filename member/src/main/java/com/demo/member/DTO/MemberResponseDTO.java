package com.demo.member.DTO;

import lombok.Data;

@Data
public class MemberResponseDTO {
    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private String createdAt;
}
