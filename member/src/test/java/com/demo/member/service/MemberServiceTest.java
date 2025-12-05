package com.demo.member.service;

import com.demo.member.DTO.LoginRequestDTO;
import com.demo.member.DTO.LoginValidationResponseDTO;
import com.demo.member.DTO.MemberRegisterRequestDTO;
import com.demo.member.DTO.MemberResponseDTO;
import com.demo.member.entity.Member;
import com.demo.member.exception.DuplicateResourceException;
import com.demo.member.exception.ResourceNotFoundException;
import com.demo.member.repository.MemberRepository;
import com.demo.member.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    private MemberRegisterRequestDTO registerRequest;
    private Member savedMember;
    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new MemberRegisterRequestDTO();
        registerRequest.setFullName("John Doe");
        registerRequest.setUserName("john@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setPhoneNumber("1234567890");
        registerRequest.setAddress("123 Main St");


        savedMember = Member.builder()
                .userId(1L)
                .fullName("John Doe")
                .userName("john@example.com")
                .phoneNumber("1234567890")
                .address("123 Main St")
                .passwordHash("$2a$10$encodedPasswordHash")
                .createdAt(LocalDateTime.now())
                .build();


        loginRequest = new LoginRequestDTO();
        loginRequest.setUserName("john@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void testRegister_Success() {

        when(memberRepository.findByUserName(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPasswordHash");
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        MemberResponseDTO result = memberService.register(registerRequest);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("John Doe", result.getFullName());
        assertEquals("john@example.com", result.getUserName());
        assertEquals("1234567890", result.getPhoneNumber());
        assertEquals("123 Main St", result.getAddress());

        verify(memberRepository, times(1)).findByUserName("john@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void testLogin_Success() {

        when(memberRepository.findByUserName("john@example.com")).thenReturn(Optional.of(savedMember));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPasswordHash")).thenReturn(true);


        LoginValidationResponseDTO result = memberService.login(loginRequest);

        assertNotNull(result);
        assertTrue(result.isMember());
        assertEquals(1L, result.getUserId());
        assertEquals("john@example.com", result.getUserName());


        verify(memberRepository, times(1)).findByUserName("john@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "$2a$10$encodedPasswordHash");
    }

    @Test
    void testLogin_UserNotFound() {
        when(memberRepository.findByUserName("john@example.com")).thenReturn(Optional.empty());


        assertThrows(ResourceNotFoundException.class, () -> {
            memberService.login(loginRequest);
        });


        verify(memberRepository, times(1)).findByUserName("john@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testLogin_InvalidPassword() {

        when(memberRepository.findByUserName("john@example.com")).thenReturn(Optional.of(savedMember));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$encodedPasswordHash")).thenReturn(false);


        loginRequest.setPassword("wrongpassword");
        assertThrows(RuntimeException.class, () -> {
            memberService.login(loginRequest);
        });

        verify(memberRepository, times(1)).findByUserName("john@example.com");
        verify(passwordEncoder, times(1)).matches("wrongpassword", "$2a$10$encodedPasswordHash");
    }

}

