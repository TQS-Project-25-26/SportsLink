package tqs.sportslink.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test") // Não carregar em testes
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Usar padrões de origem em vez de "*" quando allowCredentials é true
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "http://127.0.0.1:*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Recursos estáticos - permitir acesso público
                .requestMatchers("/", "/index.html", "/css/**", "/js/**").permitAll()
                
                // Páginas de autenticação - públicas (login, register)
                .requestMatchers("/pages/register.html").permitAll()
                
                // Outras páginas HTML - requerem autenticação
                .requestMatchers("/pages/**").authenticated()
                
                // API de rentals - pública para leitura
                .requestMatchers("GET", "/api/rentals/**").permitAll()
                .requestMatchers("POST", "/api/rentals/**").authenticated()  // criar/atualizar requer auth
                
                // Auth - público (login, register, logout)
                .requestMatchers("/api/auth/**").permitAll()
                
                // Owner endpoints - requerem autenticação
                .requestMatchers("/api/owner/**").authenticated()
                
                // Admin endpoints - requerem autenticação
                .requestMatchers("/api/admin/**").authenticated()
                
                // H2 console - apenas em desenvolvimento
                .requestMatchers("/h2-console/**").permitAll()
                
                // Outros endpoints requerem autenticação por padrão
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
