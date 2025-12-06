package tqs.sportslink.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança para TESTES (profile=test)
 * 
 * Em testes, TODAS as rotas são permitidas sem autenticação
 * para facilitar os testes de integração e unitários
 */
@TestConfiguration
@EnableWebSecurity
@Profile("test")
public class TestSecurityConfig {

    @Bean
    @Order(1) // Maior prioridade que a config em produção
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Em testes, permitir TODO o acesso sem autenticação
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
