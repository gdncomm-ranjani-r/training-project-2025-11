package com.demo.api_gateway.controller;

import com.demo.api_gateway.DTO.GdnBaseResponse;
import com.demo.api_gateway.DTO.LoginRequest;
import com.demo.api_gateway.DTO.LoginResponse;
import com.demo.api_gateway.DTO.LoginValidationResponseDTO;
import com.demo.api_gateway.Feign.MemberFeign;
import com.demo.api_gateway.service.JwtUtilService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private MemberFeign memberFeign;

    @Autowired
    private JwtUtilService jwtUtilService;

    @PostMapping("/login")
    public ResponseEntity<GdnBaseResponse<LoginResponse>> login(@RequestBody LoginRequest request) {

        log.info("Received login request for username: {}", request != null ? request.getUserName() : "null");

        if (request.getUserName() == null || request.getUserName().isEmpty() ||
                request.getPassword() == null || request.getPassword().isEmpty()) {
            log.warn("Login request failed: Username or password is missing");
            GdnBaseResponse<LoginResponse> errorResponse = GdnBaseResponse.error("Username and password are required", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            log.debug("Validating credentials with member service for username: {}", request.getUserName());
            GdnBaseResponse<LoginValidationResponseDTO> memberResponse = memberFeign.validate(request);
            
            if (memberResponse == null || !memberResponse.isSuccess() || memberResponse.getData() == null) {
                log.warn("Login failed: Member validation returned null or unsuccessful for username: {}", request.getUserName());
                GdnBaseResponse<LoginResponse> errorResponse = GdnBaseResponse.error("Invalid credentials", HttpStatus.UNAUTHORIZED.value());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            LoginValidationResponseDTO res = memberResponse.getData();
            if (!res.isMember() || res.getUserId() == null) {
                log.warn("Login failed: Invalid member or missing userId for username: {}", request.getUserName());
                GdnBaseResponse<LoginResponse> errorResponse = GdnBaseResponse.error("Invalid credentials", HttpStatus.UNAUTHORIZED.value());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            String token = jwtUtilService.generateToken(res.getUserId().toString());
            log.info("Login successful - username: {}, userId: {}, token generated", 
                    request.getUserName(), res.getUserId());

            LoginResponse loginResponse = new LoginResponse(token, res.getUserId());
            GdnBaseResponse<LoginResponse> response = GdnBaseResponse.success(loginResponse, "Login successful", HttpStatus.OK.value());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Login error for username: {}", request.getUserName(), e);
            GdnBaseResponse<LoginResponse> errorResponse = GdnBaseResponse.error("Invalid credentials", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}