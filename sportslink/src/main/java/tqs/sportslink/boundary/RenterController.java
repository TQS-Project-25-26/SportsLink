package tqs.sportslink.boundary;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import tqs.sportslink.service.RentalService;
import tqs.sportslink.service.EquipmentService;
import tqs.sportslink.service.FacilityService;
import tqs.sportslink.dto.RentalRequestDTO;
import tqs.sportslink.dto.RentalResponseDTO;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
// @PreAuthorize("hasRole('RENTER')") // Comentado para testes
public class RenterController {

    private final RentalService rentalService;
    private final EquipmentService equipmentService;
    private final FacilityService facilityService;

    public RenterController(RentalService rentalService, EquipmentService equipmentService, FacilityService facilityService) {
        this.rentalService = rentalService;
        this.equipmentService = equipmentService;
        this.facilityService = facilityService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<String>> searchFacilities(@RequestParam String location, @RequestParam String sport, @RequestParam String time) {
        List<String> facilities = facilityService.searchFacilities(location, sport, time);
        return ResponseEntity.ok(facilities);
    }

    @PostMapping("/rental")
    public ResponseEntity<RentalResponseDTO> createRental(@Valid @RequestBody RentalRequestDTO request) {
        RentalResponseDTO response = rentalService.createRental(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/rental/{id}/cancel")
    public ResponseEntity<RentalResponseDTO> cancelRental(@PathVariable Long id) {
        RentalResponseDTO response = rentalService.cancelRental(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/rental/{id}/update")
    public ResponseEntity<RentalResponseDTO> updateRental(@PathVariable Long id, @Valid @RequestBody RentalRequestDTO request) {
        RentalResponseDTO response = rentalService.updateRental(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rental/{id}/status")
    public ResponseEntity<RentalResponseDTO> getRentalStatus(@PathVariable Long id) {
        RentalResponseDTO response = rentalService.getRentalStatus(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/facility/{id}/equipments")
    public ResponseEntity<List<String>> getEquipments(@PathVariable Long id) {
        List<String> equipments = equipmentService.getEquipmentsByFacility(id);
        return ResponseEntity.ok(equipments);
    }
}
