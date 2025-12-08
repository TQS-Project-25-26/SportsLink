package tqs.sportslink.boundary;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import tqs.sportslink.dto.AuthResponseDTO;
import tqs.sportslink.dto.UserProfileDTO;
import tqs.sportslink.dto.UserRequestDTO;
import tqs.sportslink.service.AuthService;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Adiciona o token JWT como cookie HTTP-only
     */
    private void setTokenCookie(HttpServletResponse response, String token) {
        // HttpOnly: protege contra XSS (JS não consegue acessar)
        // SameSite=Lax: proteção contra CSRF
        // Max-Age: 24 horas
        String cookieValue = String.format("%s; Path=/; HttpOnly; SameSite=Lax; Max-Age=86400; Secure",
                token);
        response.addHeader("Set-Cookie", "authToken=" + cookieValue);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginUser(
            @Valid @RequestBody UserRequestDTO request,
            HttpServletResponse response) {
        AuthResponseDTO authResponse = authService.login(request);
        setTokenCookie(response, authResponse.getToken());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> registerUser(
            @Valid @RequestBody UserRequestDTO request,
            HttpServletResponse response) {
        AuthResponseDTO authResponse = authService.register(request);
        setTokenCookie(response, authResponse.getToken());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(
            @RequestHeader("Authorization") String token,
            HttpServletResponse response) {
        authService.logout(token);
        // Limpar cookie
        response.addHeader("Set-Cookie", "authToken=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        UserProfileDTO profile = authService.getProfile(token);
        return ResponseEntity.ok(profile);
    }
}

