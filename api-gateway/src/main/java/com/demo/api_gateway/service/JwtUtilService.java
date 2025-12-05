package com.demo.api_gateway.service;

import io.jsonwebtoken.Claims;

public interface JwtUtilService {
    public String generateToken(String userId);
    public Claims validate(String token);
}
