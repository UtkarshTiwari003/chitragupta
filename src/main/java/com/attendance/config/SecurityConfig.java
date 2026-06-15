package com.attendance.config;

import com.attendance.util.JwtTokenProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for JWT authentication and RBAC
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository;
    
    public SecurityConfig(
            JwtTokenProvider jwtTokenProvider,
            OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .cors()
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()
                .requestMatchers("/auth/login", "/auth/refresh").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/subjects").permitAll()
                .requestMatchers("/teacher/**").hasRole("TEACHER")
                .requestMatchers("/student/**").hasRole("STUDENT")
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        if (clientRegistrationRepository.getIfAvailable() != null) {
            http.oauth2Login(oauth2 -> oauth2.successHandler(oAuth2AuthenticationSuccessHandler));
        } else {
            http.oauth2Login(AbstractHttpConfigurer::disable);
        }
        
        return http.build();
    }
}
