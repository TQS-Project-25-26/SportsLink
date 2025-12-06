package tqs.sportslink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test") // Não carregar em testes
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .cors(cors -> cors.disable())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/pages/**").permitAll()  // Recursos estáticos
                .requestMatchers("/api/rentals/**").permitAll()  // API de rentals pública
                .requestMatchers("/api/auth/**").permitAll()  // Acesso público ao AuthController
                .requestMatchers("/api/owner/**").authenticated()   // agora requer utilizador autenticado
                .requestMatchers("/h2-console/**").permitAll()  // H2 console
                .anyRequest().authenticated()  // Outros endpoints requerem autenticação
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
