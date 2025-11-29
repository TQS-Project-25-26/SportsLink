package tqs.sportslink.boundary;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tqs.sportslink.dto.FacilityDTO;
import tqs.sportslink.service.FacilityService;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
public class PublicController {

    private final FacilityService facilityService;

    public PublicController(FacilityService facilityService) {
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
}
