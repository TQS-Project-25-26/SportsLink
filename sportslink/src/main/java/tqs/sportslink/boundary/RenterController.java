package tqs.sportslink.boundary;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tqs.sportslink.dto.FacilityDTO;
import tqs.sportslink.dto.RentalRequestDTO;
import tqs.sportslink.dto.RentalResponseDTO;
import tqs.sportslink.service.EquipmentService;
import tqs.sportslink.service.FacilityService;
import tqs.sportslink.service.RentalService;

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

    @GetMapping("/facilities")
    public ResponseEntity<List<FacilityDTO>> getAllFacilities() {
        List<FacilityDTO> facilities = facilityService.getAllActiveFacilities();
        return ResponseEntity.ok(facilities);
    }

    @GetMapping("/facilities/search")
    public ResponseEntity<List<FacilityDTO>> searchFacilities(
        @RequestParam(required = false) String location,
        @RequestParam(required = false) String sport,
        @RequestParam(required = false) String time) {
        List<FacilityDTO> facilities = facilityService.searchFacilities(location, sport, time);
        return ResponseEntity.ok(facilities);
    }

    @GetMapping("/facility/{id}")
    public ResponseEntity<FacilityDTO> getFacilityById(@PathVariable Long id) {
        FacilityDTO facility = facilityService.getFacilityById(id);
        return ResponseEntity.ok(facility);
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
