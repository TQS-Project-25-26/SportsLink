package tqs.sportslink.boundary;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.User;
import tqs.sportslink.dto.EquipmentRequestDTO;
import tqs.sportslink.dto.EquipmentResponseDTO;
import tqs.sportslink.dto.FacilityRequestDTO;
import tqs.sportslink.dto.FacilityResponseDTO;
import tqs.sportslink.service.OwnerService;

@RestController
@RequestMapping("/api/owner")
@PreAuthorize("hasRole('OWNER')")
public class OwnerController {

    private final OwnerService ownerService;
    private final UserRepository userRepository;

    public OwnerController(OwnerService ownerService, UserRepository userRepository) {
        this.ownerService = ownerService;
        this.userRepository = userRepository;
    }

    /**
     * Obtém o ID do owner autenticado com base no email presente no JWT.
     */
    private Long getAuthenticatedOwnerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        String email = authentication.getName(); // definido no JwtAuthenticationFilter
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        return user.getId();
    }

    /**
     * Valida se o ownerId do path corresponde ao utilizador autenticado.
     */
    private void validateOwnerId(Long ownerIdFromPath) {
        Long authOwnerId = getAuthenticatedOwnerId();
        if (!authOwnerId.equals(ownerIdFromPath)) {
            throw new IllegalArgumentException("Owner ID does not match authenticated user");
        }
    }

    // ============================
    // FACILITIES
    // ============================

    @PostMapping(value = "/{ownerId}/facilities", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FacilityResponseDTO> createFacility(
            @PathVariable Long ownerId,
            @org.springframework.web.bind.annotation.RequestPart("facility") FacilityRequestDTO request,
            @org.springframework.web.bind.annotation.RequestPart(value = "image", required = false) org.springframework.web.multipart.MultipartFile image) {
        validateOwnerId(ownerId);
        return ResponseEntity.ok(ownerService.createFacility(ownerId, request, image));
    }

    @GetMapping("/{ownerId}/facilities")
    public ResponseEntity<List<FacilityResponseDTO>> getOwnerFacilities(
            @PathVariable Long ownerId) {
        validateOwnerId(ownerId);
        return ResponseEntity.ok(ownerService.getFacilities(ownerId));
    }

    @PutMapping("/{ownerId}/facilities/{facilityId}")
    public ResponseEntity<FacilityResponseDTO> updateFacility(
            @PathVariable Long ownerId,
            @PathVariable Long facilityId,
            @RequestBody FacilityRequestDTO request) {
        validateOwnerId(ownerId);
        return ResponseEntity.ok(ownerService.updateFacility(ownerId, facilityId, request));
    }

    @DeleteMapping("/{ownerId}/facilities/{facilityId}")
    public ResponseEntity<Void> deleteFacility(
            @PathVariable Long ownerId,
            @PathVariable Long facilityId) {
        validateOwnerId(ownerId);
        ownerService.deleteFacility(ownerId, facilityId);
        return ResponseEntity.noContent().build();
    }

    // ============================
    // EQUIPMENT
    // ============================

    @PostMapping("/{ownerId}/facilities/{facilityId}/equipment")
    public ResponseEntity<EquipmentResponseDTO> addEquipment(
            @PathVariable Long ownerId,
            @PathVariable Long facilityId,
            @RequestBody EquipmentRequestDTO request) {
        validateOwnerId(ownerId);
        return ResponseEntity.ok(ownerService.addEquipment(ownerId, facilityId, request));
    }

    @GetMapping("/{ownerId}/facilities/{facilityId}/equipment")
    public ResponseEntity<List<EquipmentResponseDTO>> listEquipment(
            @PathVariable Long ownerId,
            @PathVariable Long facilityId) {
        validateOwnerId(ownerId);
        return ResponseEntity.ok(ownerService.getEquipment(ownerId, facilityId));
    }

    @PutMapping("/{ownerId}/equipment/{equipmentId}")
    public ResponseEntity<EquipmentResponseDTO> updateEquipment(
            @PathVariable Long ownerId,
            @PathVariable Long equipmentId,
            @RequestBody EquipmentRequestDTO request) {
        validateOwnerId(ownerId);
        return ResponseEntity.ok(ownerService.updateEquipment(ownerId, equipmentId, request));
    }
}
