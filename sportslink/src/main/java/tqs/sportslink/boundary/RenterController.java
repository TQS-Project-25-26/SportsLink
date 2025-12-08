package tqs.sportslink.boundary;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tqs.sportslink.data.model.Sport;
import tqs.sportslink.dto.EquipmentResponseDTO;
import tqs.sportslink.dto.FacilityResponseDTO;
import tqs.sportslink.dto.RentalRequestDTO;
import tqs.sportslink.dto.RentalResponseDTO;
import tqs.sportslink.service.EquipmentService;
import tqs.sportslink.service.FacilityService;
import tqs.sportslink.service.RentalService;

@RestController
@RequestMapping("/api/rentals")
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
    public ResponseEntity<List<FacilityResponseDTO>> searchFacilities(
            @RequestParam(required = false) String location, 
            @RequestParam(required = false) String sport, 
            @RequestParam(required = false) String time) {
        List<FacilityResponseDTO> facilities = facilityService.searchFacilities(location, sport, time);
        return ResponseEntity.ok(facilities);
    }

    @PostMapping("/rental")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<RentalResponseDTO> createRental(@Valid @RequestBody RentalRequestDTO request) {
        RentalResponseDTO response = rentalService.createRental(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/rental/{id}/cancel")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<RentalResponseDTO> cancelRental(@PathVariable Long id) {
        RentalResponseDTO response = rentalService.cancelRental(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/rental/{id}/update")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<RentalResponseDTO> updateRental(@PathVariable Long id, @Valid @RequestBody RentalRequestDTO request) {
        RentalResponseDTO response = rentalService.updateRental(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rental/{id}/status")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<RentalResponseDTO> getRentalStatus(@PathVariable Long id) {
        RentalResponseDTO response = rentalService.getRentalStatus(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<List<RentalResponseDTO>> getUserHistory(@RequestParam Long userId) {
        List<RentalResponseDTO> history = rentalService.getUserRentals(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/facility/{id}/equipments")
    public ResponseEntity<List<EquipmentResponseDTO>> getEquipments(@PathVariable Long id) {
        List<EquipmentResponseDTO> equipments = equipmentService.getEquipmentsByFacility(id);
        return ResponseEntity.ok(equipments);
    }

    @GetMapping("/sports")
    public ResponseEntity<List<String>> getSports() {
        List<String> sports = Arrays.stream(Sport.values()).map(Enum::name).collect(Collectors.toList());
        return ResponseEntity.ok(sports);
    }
}
