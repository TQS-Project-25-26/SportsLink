package tqs.sportslink.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import tqs.sportslink.boundary.AuthController;
import tqs.sportslink.config.TestSecurityConfig;
import tqs.sportslink.dto.AuthResponseDTO;
import tqs.sportslink.dto.UserRequestDTO;
import tqs.sportslink.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UNIT TEST para AuthController
 * Testa endpoints REST usando MockMvc e mocks do AuthService
 */
@WebMvcTest(controllers = AuthController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class UnitAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRequestDTO loginRequest;
    private UserRequestDTO registerRequest;
    private AuthResponseDTO authResponse;

    @BeforeEach
    void setUp() {
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

        // Auth response
        authResponse = new AuthResponseDTO("jwt-token-123", "RENTER");
    }

    @Test
    void whenLoginWithValidCredentials_thenReturn200AndToken() throws Exception {
        // Arrange
        when(authService.login(any(UserRequestDTO.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.role").value("RENTER"));

        verify(authService, times(1)).login(any(UserRequestDTO.class));
    }

    @Test
    void whenLoginWithInvalidEmail_thenReturn400() throws Exception {
        // Arrange
        loginRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(UserRequestDTO.class));
    }

    @Test
    void whenLoginWithMissingPassword_thenReturn400() throws Exception {
        // Arrange
        loginRequest.setPassword(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(UserRequestDTO.class));
    }

    @Test
    void whenLoginWithShortPassword_thenReturn400() throws Exception {
        // Arrange
        loginRequest.setPassword("12345");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(UserRequestDTO.class));
    }

    @Test
    void whenLoginWithWrongCredentials_thenReturn400() throws Exception {
        // Arrange
        when(authService.login(any(UserRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Invalid email or password"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, times(1)).login(any(UserRequestDTO.class));
    }

    @Test
    void whenRegisterWithValidData_thenReturn200AndToken() throws Exception {
        // Arrange
        when(authService.register(any(UserRequestDTO.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.role").value("RENTER"));

        verify(authService, times(1)).register(any(UserRequestDTO.class));
    }

    @Test
    void whenRegisterWithInvalidEmail_thenReturn400() throws Exception {
        // Arrange
        registerRequest.setEmail("not-an-email");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(UserRequestDTO.class));
    }

    @Test
    void whenRegisterWithExistingEmail_thenReturn400() throws Exception {
        // Arrange
        when(authService.register(any(UserRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Email already registered"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, times(1)).register(any(UserRequestDTO.class));
    }

    @Test
    void whenRegisterWithMissingEmail_thenReturn400() throws Exception {
        // Arrange
        registerRequest.setEmail(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(UserRequestDTO.class));
    }

    @Test
    void whenLogoutWithValidToken_thenReturn200() throws Exception {
        // Arrange
        doNothing().when(authService).logout(anyString());

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(authService, times(1)).logout("Bearer valid-token");
    }

    @Test
    void whenLogoutWithoutToken_thenReturn400() throws Exception {
        // O Spring MVC lança MissingRequestHeaderException quando header obrigatório falta
        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authService, never()).logout(anyString());
    }
}
