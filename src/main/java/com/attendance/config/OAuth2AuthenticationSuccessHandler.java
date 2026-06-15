package com.attendance.config;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.LoginResponse;
import com.attendance.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;
    private final ObjectMapper objectMapper;
    private final String authorizedRedirectUri;

    public OAuth2AuthenticationSuccessHandler(
            AuthenticationService authenticationService,
            ObjectMapper objectMapper,
            @Value("${app.oauth2.authorized-redirect-uri:}") String authorizedRedirectUri) {
        this.authenticationService = authenticationService;
        this.objectMapper = objectMapper;
        this.authorizedRedirectUri = authorizedRedirectUri;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");

        if (email == null || email.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "OAuth provider did not return an email");
            return;
        }

        LoginResponse loginResponse = authenticationService.loginWithOAuthEmail(email);

        if (authorizedRedirectUri != null && !authorizedRedirectUri.isBlank()) {
            String redirectUrl = UriComponentsBuilder.fromUriString(authorizedRedirectUri)
                .queryParam("accessToken", loginResponse.getAccessToken())
                .queryParam("refreshToken", loginResponse.getRefreshToken())
                .build()
                .toUriString();
            response.sendRedirect(redirectUrl);
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.success(loginResponse));
    }
}
