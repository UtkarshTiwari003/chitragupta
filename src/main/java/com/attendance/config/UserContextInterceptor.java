package com.attendance.config;

import com.attendance.util.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to extract userId from JWT token and set it as request attribute
 */
@Component
public class UserContextInterceptor implements HandlerInterceptor {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    public UserContextInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtTokenProvider.validateToken(token)) {
                    Long userId = jwtTokenProvider.getUserIdFromToken(token);
                    request.setAttribute("userId", userId);
                }
            }
        } catch (Exception e) {
            // Continue without userId if extraction fails
        }
        return true;
    }
}
