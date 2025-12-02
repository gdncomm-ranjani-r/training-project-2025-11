package com.demo.member.controller;

import com.demo.member.DTO.LoginRequestDTO;
import com.demo.member.DTO.LoginValidationResponseDTO;
import com.demo.member.DTO.MemberRegisterRequestDTO;
import com.demo.member.DTO.MemberResponseDTO;
import com.demo.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class MemberController {

    @Autowired
    private MemberService memberService;


    @PostMapping("/register")
    public ResponseEntity<MemberResponseDTO> register(@RequestBody MemberRegisterRequestDTO request) {
        MemberResponseDTO memberResponseDTO = memberService.register(request);
        return new ResponseEntity<>(memberResponseDTO, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginValidationResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(memberService.login(request));
    }
}
