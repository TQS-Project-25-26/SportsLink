package tqs.sportslink.C_Tests_controller;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
@Requirement("SL-23")
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
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
                authResponse = new AuthResponseDTO("jwt-token-123", java.util.Set.of("RENTER"), "RENTER", 1L);
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
                                .andExpect(jsonPath("$.roles[0]").value("RENTER"));

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
        @Requirement("SL-41")
        void whenRegister_thenReturnToken() throws Exception {
                // Arrange
                when(authService.register(any(UserRequestDTO.class))).thenReturn(authResponse);

                // Act & Assert
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                                .andExpect(jsonPath("$.roles[0]").value("RENTER"));

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
        @Requirement("SL-42")
        void whenLogoutWithoutToken_thenReturn400() throws Exception {
                // O Spring MVC lança MissingRequestHeaderException quando header obrigatório
                // falta
                // Act & Assert
                mockMvc.perform(post("/api/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest());

                verify(authService, never()).logout(anyString());
        }

        @Test
        void whenGetProfileWithValidToken_thenReturn200AndProfile() throws Exception {
                // Arrange
                tqs.sportslink.dto.UserProfileDTO profile = new tqs.sportslink.dto.UserProfileDTO(
                                1L, "test@example.com", "Test User", "123456789",
                                null, true, 0, 0, null, "RENTER");

                when(authService.getProfile("valid-token")).thenReturn(profile);

                // Act & Assert
                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/auth/profile")
                                .header("Authorization", "Bearer valid-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("test@example.com"))
                                .andExpect(jsonPath("$.role").value("RENTER"));
        }
}
