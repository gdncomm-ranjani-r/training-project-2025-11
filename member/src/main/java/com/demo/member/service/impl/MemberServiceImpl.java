package com.demo.member.service.impl;


import com.demo.member.DTO.LoginRequestDTO;
import com.demo.member.DTO.LoginValidationResponseDTO;
import com.demo.member.DTO.MemberRegisterRequestDTO;
import com.demo.member.DTO.MemberResponseDTO;
import com.demo.member.entity.Member;
import com.demo.member.repository.MemberRepository;
import com.demo.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public MemberResponseDTO register(MemberRegisterRequestDTO request) {

        if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered.");
        }

        Member user = Member.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .createdAt(LocalDateTime.now())
                .build();

        Member saved = memberRepository.save(user);
        return convertToDTO(saved);
    }

    public LoginValidationResponseDTO login(LoginRequestDTO request) {

        Member user = memberRepository.findByEmail(request.getEmail())
                                       .orElseThrow(() -> new RuntimeException("User does not exist"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        LoginValidationResponseDTO response = new LoginValidationResponseDTO();
        response.setMember(true);
        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        return response;
    }


    public static MemberResponseDTO convertToDTO(Member member) {
        MemberResponseDTO dto = new MemberResponseDTO();
        BeanUtils.copyProperties(member, dto);
        dto.setCreatedAt(member.getCreatedAt().toString());
        return dto;
    }
}
