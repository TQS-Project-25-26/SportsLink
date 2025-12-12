package tqs.sportslink.config;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Collections;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.context.SecurityContextHolder;

import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Role;
import tqs.sportslink.data.model.User;
import tqs.sportslink.service.AuthService;
import tqs.sportslink.util.JwtUtil;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock JwtUtil jwtUtil;
    @Mock UserRepository userRepository;
    @Mock AuthService authService;

    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain filterChain;

    @InjectMocks JwtAuthenticationFilter filter;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void whenBearerTokenValidAndActiveUser_thenSetsAuthenticationAndContinuesChain() throws ServletException, IOException {
        String token = "valid-token";
        String email = "user@test.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(authService.isTokenBlacklisted(token)).thenReturn(false);
        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.validateToken(token, email)).thenReturn(true);

        User user = new User();
        user.setEmail(email);
        user.setActive(true);
        user.setRoles(Collections.singleton(Role.OWNER)); // any role(s)
        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(user));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(email);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void whenTokenInCookieButBlacklisted_thenSkipsAuthenticationAndContinuesChain() throws ServletException, IOException {
        String token = "blacklisted-token";

        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("authToken", token) });
        when(authService.isTokenBlacklisted(token)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractEmail(anyString());
        verify(jwtUtil, never()).validateToken(anyString(), anyString());
    }
}
