package tqs.sportslink.service;

import org.springframework.stereotype.Service;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.dto.FacilityResponseDTO;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacilityService {

    private final FacilityRepository facilityRepository;

    public FacilityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    public List<FacilityResponseDTO> searchFacilities(String location, String sport, String time) {
        List<Facility> facilities;
        
        // Handle optional parameters
        if (location != null && !location.isEmpty() && sport != null && !sport.isEmpty()) {
            facilities = facilityRepository.findByCityAndSportType(location, sport);
        } else if (location != null && !location.isEmpty()) {
            facilities = facilityRepository.findByCity(location);
        } else if (sport != null && !sport.isEmpty()) {
            facilities = facilityRepository.findBySportType(sport);
        } else {
            // No filters - return all
            facilities = facilityRepository.findAll();
        }
        
        // Filtrar ACTIVE no service - LÓGICA DE NEGÓCIO
        return facilities.stream()
            .filter(f -> "ACTIVE".equals(f.getStatus()))
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    private FacilityResponseDTO toDTO(Facility facility) {
        FacilityResponseDTO dto = new FacilityResponseDTO(
            facility.getId(),
            facility.getName(),
            facility.getSports(),
            facility.getCity(),
            facility.getAddress(),
            facility.getDescription(),
            facility.getPricePerHour(),
            facility.getRating()
        );
        if (facility.getOpeningTime() != null) {
            dto.setOpeningTime(facility.getOpeningTime().toString());
        }
        if (facility.getClosingTime() != null) {
            dto.setClosingTime(facility.getClosingTime().toString());
        }
        return dto;
    }
}
