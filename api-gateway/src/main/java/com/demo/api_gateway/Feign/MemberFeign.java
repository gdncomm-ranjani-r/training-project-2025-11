package com.demo.api_gateway.Feign;

import com.demo.api_gateway.DTO.GdnBaseResponse;
import com.demo.api_gateway.DTO.LoginRequest;
import com.demo.api_gateway.DTO.LoginValidationResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "member", url = "http://localhost:8014")
public interface MemberFeign {
    @PostMapping("/member/login")
    GdnBaseResponse<LoginValidationResponseDTO> validate(@RequestBody LoginRequest request);
}