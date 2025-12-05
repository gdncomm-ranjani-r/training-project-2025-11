package com.demo.member.DTO;

import lombok.Data;

@Data
public class MemberRegisterRequestDTO {

    private String fullName;
    private String userName;
    private String phoneNumber;
    private String address;
    private String password;
}
