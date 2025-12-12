package tqs.sportslink.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.User;
import tqs.sportslink.service.AuthService;
import tqs.sportslink.util.JwtUtil;

@Component
@Profile("!test")  // NÃO carregar em testes
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AuthService authService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository, AuthService authService) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = resolveToken(request);

            if (isBlank(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            if (authService.isTokenBlacklisted(token)) {
                log.debug("Token is blacklisted, skipping authentication");
                filterChain.doFilter(request, response);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            String email = safeExtractEmail(token);
            if (email == null) {
                filterChain.doFilter(request, response);
                return;
            }

            if (!Boolean.TRUE.equals(jwtUtil.validateToken(token, email))) {
                filterChain.doFilter(request, response);
                return;
            }

            User user = userRepository.findByEmail(email).orElse(null);
            if (!isActiveUser(user)) {
                filterChain.doFilter(request, response);
                return;
            }

            setAuthentication(email, user);

        } catch (Exception e) {
            // Não rebentamos o request, apenas fazemos log e seguimos sem autenticação
            log.error("Error processing JWT authentication filter", e);
        }

        // Continuar a cadeia de filtros em qualquer caso
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1. Tentar obter token do header Authorization
        String token = resolveBearerToken(request);
        if (!isBlank(token)) {
            return token;
        }
        // 2. Se não encontrou no header, tentar cookie
        return resolveCookieToken(request, "authToken");
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // token "limpo"
        }
        return null;
    }

    private String resolveCookieToken(HttpServletRequest request, String cookieName) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String safeExtractEmail(String token) {
        try {
            return jwtUtil.extractEmail(token);
        } catch (Exception ex) {
            log.debug("Failed to extract email from token: {}", ex.getMessage());
            return null;
        }
    }

    private boolean isActiveUser(User user) {
        return user != null && user.getActive() != null && user.getActive();
    }

    private void setAuthentication(String email, User user) {
        List<SimpleGrantedAuthority> authorities = buildAuthorities(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,   // principal simples (email)
                        null,    // sem credenciais
                        authorities
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private List<SimpleGrantedAuthority> buildAuthorities(User user) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (user.getRoles() != null) {
            user.getRoles().forEach(role ->
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()))
            );
        }

        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_RENTER"));
        }

        return authorities;
    }

    private boolean isBlank(String token) {
        return token == null || token.isBlank();
    }
}
