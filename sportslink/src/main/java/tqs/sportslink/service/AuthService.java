package tqs.sportslink.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tqs.sportslink.dto.AuthResponseDTO;
import tqs.sportslink.dto.UserRequestDTO;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthResponseDTO login(UserRequestDTO request) {
        // Lógica mock
        logger.debug("Login attempt for user: {}", request.getEmail());
        return new AuthResponseDTO("mock-token", "RENTER");
    }

    public AuthResponseDTO register(UserRequestDTO request) {
        // Lógica mock
        logger.debug("Registration attempt for user: {}", request.getEmail());
        return new AuthResponseDTO("mock-token", "RENTER");
    }

    public void logout(String token) {
        // Lógica mock
    }
}
