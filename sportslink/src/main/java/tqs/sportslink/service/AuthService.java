package tqs.sportslink.service;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Role;
import tqs.sportslink.data.model.User;
import tqs.sportslink.dto.AuthResponseDTO;
import tqs.sportslink.dto.UserProfileDTO;
import tqs.sportslink.dto.UserRequestDTO;
import tqs.sportslink.util.JwtUtil;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Set<String> blacklistedTokens; // Simple in-memory blacklist

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.blacklistedTokens = new HashSet<>();
    }

    /**
     * Login: valida credenciais e retorna token JWT
     */
    public AuthResponseDTO login(UserRequestDTO request) {
        logger.debug("Login attempt for user: {}", request.getEmail());

        // Buscar usuário por email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // Verificar se usuário está ativo
        if (!user.getActive()) {
            throw new IllegalArgumentException("User account is inactive");
        }

        // Validar senha
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Gerar token JWT
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        logger.info("User {} logged in successfully", user.getEmail());
        return new AuthResponseDTO(token, user.getRole().name());
    }

    /**
     * Register: cria novo usuário e retorna token JWT
     */
    public AuthResponseDTO register(UserRequestDTO request) {
        logger.debug("Registration attempt for user: {}", request.getEmail());

        // Verificar se email já existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        // Criar novo usuário
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setName(request.getName() != null ? request.getName() : "User");
        newUser.setPhone(request.getPhone());
        newUser.setActive(true);

        // Definir role (default: RENTER)
        if (request.getRole() != null) {
            try {
                newUser.setRole(Role.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                newUser.setRole(Role.RENTER);
            }
        } else {
            newUser.setRole(Role.RENTER);
        }

        // Salvar usuário
        User savedUser = userRepository.save(newUser);

        // Gerar token JWT
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());

        logger.info("User {} registered successfully with role {}", savedUser.getEmail(), savedUser.getRole());
        return new AuthResponseDTO(token, savedUser.getRole().name());
    }

    /**
     * Logout: adiciona token à blacklist
     */
    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);
            blacklistedTokens.add(jwtToken);
            logger.info("Token added to blacklist");
        }
    }

    /**
     * Verifica se token está na blacklist
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /**
     * Get user profile information
     * token aqui já vem SEM "Bearer " (trimming feito no AuthController)
     */
    public UserProfileDTO getProfile(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Missing authentication token");
        }

        // Verificar se o token foi "logoutado"
        if (isTokenBlacklisted(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        String email;
        try {
            // Extrai o email do subject do JWT (não é o role!)
            email = jwtUtil.extractEmail(token);
        } catch (Exception e) {
            logger.warn("Failed to extract email from token", e);
            throw new IllegalArgumentException("Invalid or expired token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Valida se o token corresponde ao utilizador e não está expirado
        if (!jwtUtil.validateToken(token, user.getEmail())) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        // Preenche o DTO com os campos de perfil + contagem de rentals/facilities
        return new UserProfileDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getRole(),
                user.getActive(),
                user.getRentals().size(),
                user.getFacilities().size(),
                user.getCreatedAt()
        );
    }
}
