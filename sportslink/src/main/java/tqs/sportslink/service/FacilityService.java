package tqs.sportslink.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.dto.FacilityDTO;

@Service
public class FacilityService {

    private final FacilityRepository facilityRepository;

    public FacilityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    public List<FacilityDTO> searchFacilities(String location, String sport, String time) {
        List<Facility> facilities;
        
        // Usar queries específicas do repository para eficiência
        if (location != null && !location.trim().isEmpty() && sport != null && !sport.trim().isEmpty()) {
            facilities = facilityRepository.findByCityAndSportType(location.trim(), sport.trim());
        } else if (location != null && !location.trim().isEmpty()) {
            facilities = facilityRepository.findByCity(location.trim());
        } else if (sport != null && !sport.trim().isEmpty()) {
            facilities = facilityRepository.findBySportType(sport.trim());
        } else {
            // Sem filtros, buscar todas
            facilities = facilityRepository.findAll();
        }
        
        // Filtrar apenas ativas e mapear para DTO
        return facilities.stream()
            .filter(f -> "ACTIVE".equals(f.getStatus()))
            .map(f -> new FacilityDTO(
                f.getId(),
                f.getName(),
                f.getSportType(),
                f.getAddress(),
                f.getCity(),
                f.getDescription(),
                f.getPricePerHour(),
                f.getRating(),
                f.getStatus(),
                f.getOpeningTime(),
                f.getClosingTime()
            ))
            .collect(Collectors.toList());
    }

    public List<FacilityDTO> getAllActiveFacilities() {
        List<Facility> facilities = facilityRepository.findAll();
        return facilities.stream()
            .filter(f -> "ACTIVE".equals(f.getStatus()))
            .map(f -> new FacilityDTO(
                f.getId(),
                f.getName(),
                f.getSportType(),
                f.getAddress(),
                f.getCity(),
                f.getDescription(),
                f.getPricePerHour(),
                f.getRating(),
                f.getStatus(),
                f.getOpeningTime(),
                f.getClosingTime()
            ))
            .collect(Collectors.toList());
    }

    public FacilityDTO getFacilityById(Long id) {
        Optional<Facility> facilityOpt = facilityRepository.findById(id);
        if (facilityOpt.isPresent()) {
            Facility f = facilityOpt.get();
            if ("ACTIVE".equals(f.getStatus())) {
                return new FacilityDTO(
                    f.getId(),
                    f.getName(),
                    f.getSportType(),
                    f.getAddress(),
                    f.getCity(),
                    f.getDescription(),
                    f.getPricePerHour(),
                    f.getRating(),
                    f.getStatus(),
                    f.getOpeningTime(),
                    f.getClosingTime()
                );
            }
        }
        return null;
    }
}
