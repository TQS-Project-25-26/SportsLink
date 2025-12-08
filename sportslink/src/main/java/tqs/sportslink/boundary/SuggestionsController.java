package tqs.sportslink.boundary;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.sportslink.dto.EquipmentSuggestionDTO;
import tqs.sportslink.dto.FacilitySuggestionDTO;
import tqs.sportslink.dto.OwnerSuggestionDTO;
import tqs.sportslink.dto.UserSuggestionsDTO;
import tqs.sportslink.service.IntelligentEngineService;

import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
@CrossOrigin(origins = "*")
public class SuggestionsController {

    private final IntelligentEngineService intelligentEngineService;

    public SuggestionsController(IntelligentEngineService intelligentEngineService) {
        this.intelligentEngineService = intelligentEngineService;
    }

    /**
     * GET /api/suggestions/facilities/{userId}
     * Get personalized facility suggestions for a user
     * Optional: Include latitude and longitude for location-based suggestions
     */
    @GetMapping("/facilities/{userId}")
    public ResponseEntity<List<FacilitySuggestionDTO>> getFacilitySuggestions(
            @PathVariable Long userId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        List<FacilitySuggestionDTO> suggestions = 
            intelligentEngineService.suggestFacilitiesForUser(userId, latitude, longitude);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * GET /api/suggestions/equipment/{facilityId}
     * Get equipment suggestions for a specific facility and sport
     */
    @GetMapping("/equipment/{facilityId}")
    public ResponseEntity<List<EquipmentSuggestionDTO>> getEquipmentSuggestions(
            @PathVariable Long facilityId,
            @RequestParam String sport) {
        List<EquipmentSuggestionDTO> suggestions = 
            intelligentEngineService.suggestEquipmentForSport(facilityId, sport);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * GET /api/suggestions/owner/{ownerId}
     * Get improvement suggestions for a facility owner
     */
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<OwnerSuggestionDTO>> getOwnerSuggestions(@PathVariable Long ownerId) {
        List<OwnerSuggestionDTO> suggestions = 
            intelligentEngineService.suggestImprovementsForOwner(ownerId);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * GET /api/suggestions/user/{userId}
     * Get combined suggestions for a user (facilities + equipment for current context)
     * Optional: Include latitude and longitude for location-based facility suggestions
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserSuggestionsDTO> getUserSuggestions(
            @PathVariable Long userId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) String sport) {
        
        List<FacilitySuggestionDTO> facilitySuggestions = 
            intelligentEngineService.suggestFacilitiesForUser(userId, latitude, longitude);
        
        List<EquipmentSuggestionDTO> equipmentSuggestions = List.of();
        if (facilityId != null && sport != null) {
            equipmentSuggestions = 
                intelligentEngineService.suggestEquipmentForSport(facilityId, sport);
        }
        
        UserSuggestionsDTO response = new UserSuggestionsDTO(
            facilitySuggestions,
            equipmentSuggestions
        );
        
        return ResponseEntity.ok(response);
    }
}
