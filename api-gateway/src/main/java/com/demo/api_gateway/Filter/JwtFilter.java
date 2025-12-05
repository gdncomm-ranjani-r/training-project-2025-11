package com.demo.api_gateway.Filter;

import com.demo.api_gateway.service.JwtUtilServiceImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@Slf4j
@Component
public class JwtFilter implements Filter {

    @Autowired
    private JwtUtilServiceImpl jwtUtilServiceImpl;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${member.service.url}")
    private String memberServiceUrl;

    @Value("${product.service.url}")
    private String productServiceUrl;

    @Value("${cart.service.url}")
    private String cartServiceUrl;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI();

        if (path.startsWith("/auth/")) {
            chain.doFilter(req, res);
            return;
        }

        String targetUrl = determineTargetUrl(path);
        if (targetUrl == null) {
            chain.doFilter(req, res);
            return;
        }

        String userId = null;
        if (requiresAuthentication(path)) {
            String token = getToken(request);
            
            if (token == null || token.isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "JWT token is required. Please provide Authorization: Bearer <token> or jwt cookie");
                return;
            }

            try {
                Claims claims = jwtUtilServiceImpl.validate(token);
                userId = claims.getSubject();
                if (userId == null || userId.isEmpty()) {
                    throw new Exception("Invalid token: user ID not found");
                }
            } catch (Exception e) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired JWT token");
                return;
            }
        }
        forwardRequest(request, response, targetUrl, userId);
    }

    private String determineTargetUrl(String path) {
        if (path.startsWith("/member/")) {
            return memberServiceUrl;
        } else if (path.startsWith("/books/") || path.startsWith("/books")) {
            return productServiceUrl;
        } else if (path.startsWith("/cart/") || path.startsWith("/cart")) {
            return cartServiceUrl;
        }
        return null;
    }

    private boolean requiresAuthentication(String path) {
        // Public endpoints that don't require JWT authentication
        if (path.startsWith("/auth/login") ||
            path.startsWith("/member/register") ||
            path.startsWith("/member/login") ||
            path.startsWith("/books/") ||
            path.startsWith("/books")) {
            return false;
        }
        // All other endpoints require authentication (including /member/{userId} for profile)
        return true;
    }

    private String getToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void forwardRequest(HttpServletRequest request, HttpServletResponse response,
                               String baseUrl, String userId) throws IOException {
        
        String requestPath = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullPath = queryString != null ? requestPath + "?" + queryString : requestPath;
        String targetUrl = baseUrl + fullPath;

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!"host".equalsIgnoreCase(headerName) && 
                !"content-length".equalsIgnoreCase(headerName) &&
                !"connection".equalsIgnoreCase(headerName)) {
                Enumeration<String> headerValues = request.getHeaders(headerName);
                while (headerValues.hasMoreElements()) {
                    headers.add(headerName, headerValues.nextElement());
                }
            }
        }
        
        if (userId != null && !userId.isEmpty()) {
            headers.set("X-User-Id", userId);
        }

        String requestBody = null;
        if (request.getContentLengthLong() > 0 && 
            (request.getMethod().equals("POST") || request.getMethod().equals("PUT"))) {
            try {
                requestBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
                if (requestBody != null && !requestBody.isEmpty()) {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                }
            } catch (IOException e) {
            }
        }

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
        
        try {
            ResponseEntity<String> serviceResponse = restTemplate.exchange(
                    targetUrl, httpMethod, requestEntity, String.class);

            response.setStatus(serviceResponse.getStatusCode().value());
            serviceResponse.getHeaders().forEach((key, values) -> {
                if (!"transfer-encoding".equalsIgnoreCase(key)) {
                    values.forEach(value -> response.addHeader(key, value));
                }
            });

            if (serviceResponse.getHeaders().getContentType() != null) {
                response.setContentType(serviceResponse.getHeaders().getContentType().toString());
            }

            if (serviceResponse.getBody() != null) {
                response.getWriter().write(serviceResponse.getBody());
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            response.setStatus(e.getStatusCode().value());
            response.setContentType("application/json");
            String errorBody = e.getResponseBodyAsString();
            response.getWriter().write(errorBody != null ? errorBody : 
                "{\"success\":false,\"message\":\"" + e.getMessage() + "\",\"status\":" + e.getStatusCode().value() + "}");
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            response.setStatus(e.getStatusCode().value());
            response.setContentType("application/json");
            String errorBody = e.getResponseBodyAsString();
            response.getWriter().write(errorBody != null ? errorBody : 
                "{\"success\":false,\"message\":\"" + e.getMessage() + "\",\"status\":" + e.getStatusCode().value() + "}");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\",\"status\":" + status + "}");
    }
}
