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
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test") // Não carregar em testes
public class SecurityConfig {

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                // Usar padrões de origem em vez de "*" quando allowCredentials é true
                configuration.setAllowedOriginPatterns(Arrays.asList("*"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
                        throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(authz -> authz
                                                // Recursos estáticos - permitir acesso público
                                                .requestMatchers("/", "/index.html", "/css/**", "/js/**",
                                                                "/favicon.ico", "/images/**")
                                                .permitAll()

                                                // Páginas de autenticação - públicas (login, register)
                                                .requestMatchers("/pages/register.html").permitAll()

                                                // Páginas HTML - permitir carregamento (segurança feita na API e JS)
                                                .requestMatchers("/pages/**").permitAll()

                                                // API de rentals - pública para leitura
                                                .requestMatchers(HttpMethod.GET, "/api/rentals/**").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/rentals/**").authenticated() // criar/atualizar
                                                                                                                     // requer
                                                                                                                     // auth

                                                // Auth - público (login, register, logout)
                                                .requestMatchers("/api/auth/**").permitAll()

                                                // Equipment suggestions - public (needed for booking page)
                                                .requestMatchers("/api/suggestions/equipment/**").permitAll()
                                                // Personalized suggestions - require authentication
                                                .requestMatchers("/api/suggestions/user/**",
                                                                "/api/suggestions/owner/**",
                                                                "/api/suggestions/facilities/**")
                                                .authenticated()

                                                // Stripe payments
                                                .requestMatchers("/api/payments/webhook").permitAll() // Stripe webhook
                                                                                                      // must be public
                                                .requestMatchers("/api/payments/config").permitAll() // Frontend needs
                                                                                                     // publishable key
                                                .requestMatchers("/api/payments/**").authenticated() // Other payment
                                                                                                     // endpoints
                                                                                                     // require auth

                                                // Error page - permitir acesso para mostrar erros corretamente
                                                .requestMatchers("/error").permitAll()

                                                // Owner endpoints - requerem autenticação
                                                .requestMatchers("/api/owner/**").authenticated()

                                                // Admin endpoints - requerem autenticação
                                                .requestMatchers("/api/admin/**").authenticated()

                                                // H2 console - apenas em desenvolvimento
                                                .requestMatchers("/h2-console/**").permitAll()

                                                // Outros endpoints requerem autenticação por padrão
                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
