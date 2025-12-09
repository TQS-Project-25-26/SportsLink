package tqs.sportslink.boundary;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Rental;
import tqs.sportslink.data.model.User;
import tqs.sportslink.service.AdminService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<User> updateUserStatus(@PathVariable Long id, @RequestParam Boolean active) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, active));
    }

    @GetMapping("/facilities")
    public ResponseEntity<List<Facility>> getAllFacilities() {
        return ResponseEntity.ok(adminService.getAllFacilities());
    }

    @DeleteMapping("/facilities/{id}")
    public ResponseEntity<Void> deleteFacility(@PathVariable Long id) {
        adminService.deleteFacility(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    @GetMapping("/rentals")
    public ResponseEntity<List<Rental>> getAllRentals() {
        return ResponseEntity.ok(adminService.getAllRentals());
    }

    @GetMapping("/stats/charts")
    public ResponseEntity<Map<String, Object>> getChartStats() {
        Map<String, Object> charts = Map.of(
                "rentalsBySport", adminService.getRentalsBySport(),
                "rentalsByStatus", adminService.getRentalsByStatus());
        return ResponseEntity.ok(charts);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserDetails(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserDetails(id));
    }

    @GetMapping("/facilities/{id}")
    public ResponseEntity<Facility> getFacilityDetails(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getFacilityDetails(id));
    }

    @PostMapping("/rentals/{id}/cancel")
    public ResponseEntity<Rental> cancelRental(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.cancelRental(id));
    }
}
