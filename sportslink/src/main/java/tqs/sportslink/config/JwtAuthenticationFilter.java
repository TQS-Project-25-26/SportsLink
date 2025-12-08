package tqs.sportslink.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = null;
            
            // 1. Tentar obter token do header Authorization
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7); // token "limpo"
            }
            
            // 2. Se não encontrou no header, tentar cookie
            if (token == null) {
                jakarta.servlet.http.Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (jakarta.servlet.http.Cookie cookie : cookies) {
                        if ("authToken".equals(cookie.getName())) {
                            token = cookie.getValue();
                            break;
                        }
                    }
                }
            }

            if (token != null && !token.isBlank()) {
                // Verificar se o token foi "logoutado"
                if (!authService.isTokenBlacklisted(token)) {
                    String email = null;
                    try {
                        email = jwtUtil.extractEmail(token);
                    } catch (Exception ex) {
                        logger.debug("Failed to extract email from token: {}", ex.getMessage());
                    }

                    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        // Validar token
                        if (jwtUtil.validateToken(token, email)) {
                            User user = userRepository.findByEmail(email).orElse(null);

                            if (user != null && user.getActive() != null && user.getActive()) {
                                // ROLE_OWNER, ROLE_RENTER, ROLE_ADMIN
                                java.util.List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
                                if (user.getRoles() != null) {
                                    user.getRoles().forEach(role -> 
                                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()))
                                    );
                                }
                                
                                if (authorities.isEmpty()) {
                                    authorities.add(new SimpleGrantedAuthority("ROLE_RENTER"));
                                }

                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                email,   // principal simples (email)
                                                null,    // sem credenciais
                                                authorities
                                        );

                                SecurityContextHolder.getContext().setAuthentication(authentication);
                            }
                        }
                    }
                } else {
                    logger.debug("Token is blacklisted, skipping authentication");
                }
            }
        } catch (Exception e) {
            // Não rebentamos o request, apenas fazemos log e seguimos sem autenticação
            logger.error("Error processing JWT authentication filter", e);
        }

        // Continuar a cadeia de filtros em qualquer caso
        filterChain.doFilter(request, response);
    }
}
