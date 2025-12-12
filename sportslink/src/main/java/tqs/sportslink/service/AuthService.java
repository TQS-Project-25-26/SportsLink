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

    private static final String OWNER = "OWNER";
    private static final String ADMIN = "ADMIN";
    private static final String RENTER = "RENTER";

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
            logger.warn("Login failed: invalid password for user {}", request.getEmail());
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Gerar token JWT
        Set<String> roleNames = new HashSet<>();
        user.getRoles().forEach(r -> roleNames.add(r.name()));
        
        String token = jwtUtil.generateToken(user.getEmail(), roleNames);
        
        String primaryRole = roleNames.contains(OWNER) ? OWNER : (roleNames.contains(ADMIN) ? ADMIN : RENTER);

        logger.info("User {} logged in successfully", user.getEmail());
        return new AuthResponseDTO(token, roleNames, primaryRole, user.getId());
    }

    /**
     * Register: cria novo usuário e retorna token JWT
     */
    public AuthResponseDTO register(UserRequestDTO request) {
        logger.debug("Registration attempt for user: {}", request.getEmail());

        // Verificar se email já existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already registered");
        }
        
        // Criar novo usuário
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setName(request.getName() != null ? request.getName() : "User");
        newUser.setPhone(request.getPhone());
        newUser.setActive(true);

        // Definir roles
        // Definir roles com base na lógica solicitada:
        // ADMIN -> Apenas ADMIN
        // OWNER -> OWNER e RENTER
        // Default/RENTER -> Apenas RENTER
        
        if (request.getRole() != null) {
            try {
                Role requestedRole = Role.valueOf(request.getRole().toUpperCase());
                
                if (requestedRole == Role.ADMIN) {
                    newUser.getRoles().add(Role.ADMIN);
                } else if (requestedRole == Role.OWNER) {
                    newUser.getRoles().add(Role.OWNER);
                    newUser.getRoles().add(Role.RENTER);
                } else {
                    newUser.getRoles().add(Role.RENTER);
                }
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid role requested: {}. Defaulting to RENTER.", request.getRole());
                newUser.getRoles().add(Role.RENTER);
            }
        } else {
            // Default role
            newUser.getRoles().add(Role.RENTER);
        }

        // Salvar usuário
        User savedUser = userRepository.save(newUser);

        // Gerar token JWT
        Set<String> roleNames = new HashSet<>();
        savedUser.getRoles().forEach(r -> roleNames.add(r.name()));

        String token = jwtUtil.generateToken(savedUser.getEmail(), roleNames);
        
        String primaryRole = roleNames.contains(OWNER) ? OWNER : (roleNames.contains(ADMIN) ? ADMIN : RENTER);

        logger.info("User {} registered successfully with roles {}", savedUser.getEmail(), savedUser.getRoles());
        return new AuthResponseDTO(token, roleNames, primaryRole, savedUser.getId());
    }

    /**
     * Logout: adiciona token à blacklist (apenas a parte útil do token, sem "Bearer ")
     */
    public void logout(String authHeader) {
        if (authHeader != null && !authHeader.isBlank()) {
            // Remove "Bearer " prefix if present
            String token = authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) 
                : authHeader;
            
            if (!token.isBlank()) {
                blacklistedTokens.add(token);
                logger.info("Token added to blacklist for logout");
            }
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

        // Determine primary role for backward compatibility
        String primaryRole = RENTER; // Default
        Set<String> roleNames = new HashSet<>();
        user.getRoles().forEach(r -> roleNames.add(r.name()));
        
        if (roleNames.contains(ADMIN)) {
            primaryRole = ADMIN;
        } else if (roleNames.contains(OWNER)) {
            primaryRole = OWNER;
        }

        // Preenche o DTO com os campos de perfil + contagem de rentals/facilities
        return new UserProfileDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getRoles(),
                user.getActive(),
                user.getRentals().size(),
                user.getFacilities().size(),
                user.getCreatedAt(),
                primaryRole
        );
    }
}
