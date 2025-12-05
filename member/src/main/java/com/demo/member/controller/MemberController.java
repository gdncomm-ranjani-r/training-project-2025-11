package com.demo.member.controller;

import com.demo.member.DTO.GdnBaseResponse;
import com.demo.member.DTO.LoginRequestDTO;
import com.demo.member.DTO.LoginValidationResponseDTO;
import com.demo.member.DTO.MemberRegisterRequestDTO;
import com.demo.member.DTO.MemberResponseDTO;
import com.demo.member.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService;


    @PostMapping("/register")
    public ResponseEntity<GdnBaseResponse<MemberResponseDTO>> register(@RequestBody MemberRegisterRequestDTO request) {
        log.info("Received registration request for email: {}", request != null ? request.getUserName() : "null");
        if (request == null) {
            log.warn("Registration request failed: Request body is null");
            throw new IllegalArgumentException("Request body is required");
        }
        if (request.getUserName() == null || request.getUserName().trim().isEmpty()) {
            log.warn("Registration request failed: Email is missing");
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            log.warn("Registration request failed: Password is missing for email: {}", request.getUserName());
            throw new IllegalArgumentException("Password is required");
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            log.warn("Registration request failed: Full name is missing for email: {}", request.getUserName());
            throw new IllegalArgumentException("Full name is required");
        }

        try {
            MemberResponseDTO memberResponseDTO = memberService.register(request);
            log.info("User registered successfully with userId: {} and email: {}",
                    memberResponseDTO.getUserId(), memberResponseDTO.getUserName());
            GdnBaseResponse<MemberResponseDTO> response = GdnBaseResponse.success(memberResponseDTO, "User registered successfully", HttpStatus.OK.value());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error during user registration for email: {}", request.getUserName(), e);
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<GdnBaseResponse<LoginValidationResponseDTO>> login(@RequestBody LoginRequestDTO request) {
        log.info("Received login request for username: {}", request != null ? request.getUserName() : "null");

        if (request == null) {
            log.warn("Login request failed: Request body is null");
            throw new IllegalArgumentException("Request body is required");
        }
        if (request.getUserName() == null || request.getUserName().trim().isEmpty()) {
            log.warn("Login request failed: Username is missing");
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            log.warn("Login request failed: Password is missing for username: {}", request.getUserName());
            throw new IllegalArgumentException("Password is required");
        }

        try {
            LoginValidationResponseDTO loginResponse = memberService.login(request);
            log.info("Login successful for username: {}, userId: {}",
                    request.getUserName(), loginResponse.getUserId());
            GdnBaseResponse<LoginValidationResponseDTO> response = GdnBaseResponse.success(loginResponse, "Login successful", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for username: {}", request.getUserName(), e);
            throw e;
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<GdnBaseResponse<MemberResponseDTO>> getProfile(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id") Long tokenUserId) {
        
        log.info("Received request to get profile for userId: {}, tokenUserId: {}", userId, tokenUserId);
        
        if (userId == null) {
            log.warn("Get profile request failed: User ID is missing");
            throw new IllegalArgumentException("User ID is required");
        }
        
        if (tokenUserId == null) {
            log.warn("Get profile request failed: X-User-Id header is missing");
            throw new IllegalArgumentException("X-User-Id header is required");
        }
        
        if (!userId.equals(tokenUserId)) {
            log.warn("Unauthorized access attempt: User {} tried to access profile of user {}", tokenUserId, userId);
            throw new IllegalArgumentException("You can only view your own profile");
        }
        
        try {
            MemberResponseDTO profile = memberService.getProfile(userId);
            log.info("Profile retrieved successfully for userId: {}", userId);
            GdnBaseResponse<MemberResponseDTO> response = GdnBaseResponse.success(profile, "Profile retrieved successfully", HttpStatus.OK.value());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving profile for userId: {}", userId, e);
            throw e;
        }
    }
}

