
package tqs.sportslink.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private java.util.Set<String> roles;
    private String role; // Backward compatibility
    private Long userId;
}