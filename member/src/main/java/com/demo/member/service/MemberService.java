package com.demo.member.service;

import com.demo.member.DTO.LoginRequestDTO;
import com.demo.member.DTO.LoginValidationResponseDTO;
import com.demo.member.DTO.MemberRegisterRequestDTO;
import com.demo.member.DTO.MemberResponseDTO;

public interface MemberService {
    MemberResponseDTO register(MemberRegisterRequestDTO request);
    LoginValidationResponseDTO login(LoginRequestDTO request);
}
