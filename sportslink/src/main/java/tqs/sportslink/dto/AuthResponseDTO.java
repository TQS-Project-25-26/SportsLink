
package tqs.sportslink.dto;

public class AuthResponseDTO {
    private String token;
    private String role;

    public AuthResponseDTO(String token, String role) {
        this.token = token;
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}