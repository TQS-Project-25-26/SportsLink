package tqs.sportslink.service;

import org.springframework.stereotype.Service;
import tqs.sportslink.dto.AuthResponseDTO;
import tqs.sportslink.dto.UserRequestDTO;

@Service
public class AuthService {

    public AuthResponseDTO login(UserRequestDTO request) {
        // Lógica mock
        AuthResponseDTO response = new AuthResponseDTO("mock-token", "RENTER");
        return response;
    }

    public AuthResponseDTO register(UserRequestDTO request) {
        // Lógica mock
        AuthResponseDTO response = new AuthResponseDTO("mock-token", "RENTER");
        return response;
    }

    public void logout(String token) {
        // Lógica mock
    }
}
