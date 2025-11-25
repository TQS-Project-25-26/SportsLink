package tqs.sportslink.boundary;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/rental")
@PreAuthorize("hasRole('RENTER')")
public class UserController {


    @PostMapping
    public ResponseEntity<UserResponseDTO> rental(@Valid @RequestBody UserRequestDTO request) {
        UserResponseDTO response = authService.login(request);  // Deve retornar token com role
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{token}/cancel")
    public ResponseEntity<UserResponseDTO> cancelRental(@Valid @RequestBody UserRequestDTO request) {
        UserResponseDTO response = authService.login(request);  // Deve retornar token com role
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{token}/update")
    public ResponseEntity<UserResponseDTO> cancelRental(@Valid @RequestBody UserRequestDTO request) {
        UserResponseDTO response = authService.login(request);  // Deve retornar token com role
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{token}/status")
    public ResponseEntity<UserResponseDTO> rentalStatus(@Valid @RequestBody UserRequestDTO request) {
        UserResponseDTO response = authService.login(request);  // Deve retornar token com role
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{token}/listEquipments")
    public ResponseEntity<UserResponseDTO> listEquipments(@Valid @RequestBody UserRequestDTO request) {
        UserResponseDTO response = authService.login(request);  // Deve retornar token com role
        return ResponseEntity.ok(response);
    }





    
}
