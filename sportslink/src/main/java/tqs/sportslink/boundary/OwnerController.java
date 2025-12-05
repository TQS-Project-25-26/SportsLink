// TODO: Como o projeto ainda não tem JWT real implementado, iremos temporariamente receber o ownerId no request.
// Quando mais tarde implementarmos o token, basta substituir isso pelo SecurityContext.

package tqs.sportslink.boundary;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import tqs.sportslink.dto.FacilityRequestDTO;
import tqs.sportslink.dto.FacilityResponseDTO;
import tqs.sportslink.dto.EquipmentRequestDTO;
import tqs.sportslink.dto.EquipmentResponseDTO;
import tqs.sportslink.service.OwnerService;

import java.util.List;

@RestController
@RequestMapping("/api/owner")
// @PreAuthorize("hasRole('OWNER')")    #temp para testes sem JWT
@CrossOrigin(origins = "*")
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    // ============================
    // FACILITIES
    // ============================

    @PostMapping("/{ownerId}/facilities")
    public ResponseEntity<FacilityResponseDTO> createFacility(
            @PathVariable Long ownerId,
            @RequestBody FacilityRequestDTO request
    ) {
        return ResponseEntity.ok(ownerService.createFacility(ownerId, request));
    }

    @GetMapping("/{ownerId}/facilities")
    public ResponseEntity<List<FacilityResponseDTO>> getOwnerFacilities(
            @PathVariable Long ownerId
    ) {
        return ResponseEntity.ok(ownerService.getFacilities(ownerId));
    }

    @PutMapping("/{ownerId}/facilities/{facilityId}")
    public ResponseEntity<FacilityResponseDTO> updateFacility(
            @PathVariable Long ownerId,
            @PathVariable Long facilityId,
            @RequestBody FacilityRequestDTO request
    ) {
        return ResponseEntity.ok(ownerService.updateFacility(ownerId, facilityId, request));
    }

    // ============================
    // EQUIPMENT
    // ============================

    @PostMapping("/{ownerId}/facilities/{facilityId}/equipment")
    public ResponseEntity<EquipmentResponseDTO> addEquipment(
            @PathVariable Long ownerId,
            @PathVariable Long facilityId,
            @RequestBody EquipmentRequestDTO request
    ) {
        return ResponseEntity.ok(ownerService.addEquipment(ownerId, facilityId, request));
    }

    @GetMapping("/{ownerId}/facilities/{facilityId}/equipment")
    public ResponseEntity<List<EquipmentResponseDTO>> listEquipment(
            @PathVariable Long ownerId,
            @PathVariable Long facilityId
    ) {
        return ResponseEntity.ok(ownerService.getEquipment(ownerId, facilityId));
    }

    @PutMapping("/{ownerId}/equipment/{equipmentId}")
    public ResponseEntity<EquipmentResponseDTO> updateEquipment(
            @PathVariable Long ownerId,
            @PathVariable Long equipmentId,
            @RequestBody EquipmentRequestDTO request
    ) {
        return ResponseEntity.ok(ownerService.updateEquipment(ownerId, equipmentId, request));
    }
}

