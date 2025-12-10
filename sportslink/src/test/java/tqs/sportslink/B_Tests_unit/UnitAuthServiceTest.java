package tqs.sportslink.B_Tests_unit;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Role;
import tqs.sportslink.data.model.User;
import tqs.sportslink.dto.AuthResponseDTO;
import tqs.sportslink.dto.UserRequestDTO;
import tqs.sportslink.service.AuthService;
import tqs.sportslink.util.JwtUtil;

/**
 * UNIT TEST para AuthService
 * Testa login, register e logout usando Mockito
 */
@ExtendWith(MockitoExtension.class)
@Requirement("SL-23")
class UnitAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private BCryptPasswordEncoder passwordEncoder;
    private User mockUser;
    private UserRequestDTO loginRequest;
    private UserRequestDTO registerRequest;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        // Mock user existente
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setPassword(passwordEncoder.encode("password123"));
        mockUser.setName("Test User");
        mockUser.getRoles().add(Role.RENTER);
        mockUser.setActive(true);

        // Login request
        loginRequest = new UserRequestDTO();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        // Register request
        registerRequest = new UserRequestDTO();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("newpass123");
        registerRequest.setName("New User");
        registerRequest.setRole("RENTER");
    }

    @Test
    @Requirement("SL-42")
    void whenLoginWithValidCredentials_thenReturnToken() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken(anyString(), any())).thenReturn("mock-jwt-token");

        // Act
        AuthResponseDTO response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertTrue(response.getRoles().contains("RENTER"));
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(jwtUtil, times(1)).generateToken(anyString(), any());
    }

    @Test
    void whenLoginWithInvalidEmail_thenThrowException() {
        // Arrange
        when(userRepository.findByEmail("invalid@example.com")).thenReturn(Optional.empty());
        loginRequest.setEmail("invalid@example.com");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("invalid@example.com");
        verify(jwtUtil, never()).generateToken(anyString(), any());
    }

    @Test
    void whenLoginWithInvalidPassword_thenThrowException() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(jwtUtil, never()).generateToken(anyString(), any());
    }

    @Test
    void whenLoginWithInactiveUser_thenThrowException() {
        // Arrange
        mockUser.setActive(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("User account is inactive", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(jwtUtil, never()).generateToken(anyString(), any());
    }

    @Test
    @Requirement("SL-41")
    void whenRegisterWithNewEmail_thenCreateUserAndReturnToken() {
        // Arrange
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });
        when(jwtUtil.generateToken(anyString(), any())).thenReturn("new-jwt-token");

        // Act
        AuthResponseDTO response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("new-jwt-token", response.getToken());
        assertTrue(response.getRoles().contains("RENTER"));
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtUtil, times(1)).generateToken(anyString(), any());
    }

    @Test
    void whenRegisterWithExistingEmail_thenThrowException() {
        // Arrange
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(userRepository, never()).save(any(User.class));
        verify(jwtUtil, never()).generateToken(anyString(), any());
    }

    @Test
    void whenRegisterWithOwnerRole_thenCreateOwnerUser() {
        // Arrange
        registerRequest.setRole("OWNER");
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(3L);
            return user;
        });
        when(jwtUtil.generateToken(anyString(), any())).thenReturn("owner-jwt-token");

        // Act
        AuthResponseDTO response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("owner-jwt-token", response.getToken());
        assertTrue(response.getRoles().contains("OWNER"));
        assertTrue(response.getRoles().contains("RENTER"));
        verify(userRepository, times(1))
                .save(argThat(user -> user.getRoles().contains(Role.OWNER) && user.getRoles().contains(Role.RENTER)));
    }

    @Test
    void whenRegisterWithInvalidRole_thenDefaultToRenter() {
        // Arrange
        registerRequest.setRole("INVALID_ROLE");
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(4L);
            return user;
        });
        when(jwtUtil.generateToken(anyString(), any())).thenReturn("default-jwt-token");

        // Act
        AuthResponseDTO response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.getRoles().contains("RENTER"));
        verify(userRepository, times(1)).save(argThat(user -> user.getRoles().contains(Role.RENTER)));
    }

    @Test
    void whenRegisterWithoutName_thenUseDefaultName() {
        // Arrange
        registerRequest.setName(null);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(5L);
            return user;
        });
        when(jwtUtil.generateToken(anyString(), any())).thenReturn("jwt-token");

        // Act
        AuthResponseDTO response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        verify(userRepository, times(1)).save(argThat(user -> "User".equals(user.getName())));
    }

    @Test
    @Requirement("SL-42")
    void whenLogout_thenAddTokenToBlacklist() {
        // Arrange
        String token = "Bearer some-jwt-token";

        // Act
        authService.logout(token);

        // Assert
        assertTrue(authService.isTokenBlacklisted("some-jwt-token"));
    }

    @Test
    void whenLogoutWithoutBearerPrefix_thenAddTokenToBlacklist() {
        // Arrange
        String token = "invalid-token";

        // Act
        authService.logout(token);

        // Assert
        assertTrue(authService.isTokenBlacklisted("invalid-token"));
    }

    @Test
    void whenCheckBlacklistedToken_thenReturnCorrectStatus() {
        // Arrange
        String token = "Bearer blacklisted-token";

        // Act
        authService.logout(token);

        // Assert
        assertTrue(authService.isTokenBlacklisted("blacklisted-token"));
        assertFalse(authService.isTokenBlacklisted("valid-token"));
    }

    @Test
    void whenGetProfile_thenReturnUserProfileWithRole() {
        // Arrange
        String token = "valid-token";
        String email = "test@example.com";
        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(jwtUtil.validateToken(token, email)).thenReturn(true);

        // Act
        tqs.sportslink.dto.UserProfileDTO profile = authService.getProfile(token);

        // Assert
        assertNotNull(profile);
        assertEquals("RENTER", profile.role());
        assertEquals(email, profile.email());
    }
}
