package tqs.sportslink.service;

import org.springframework.stereotype.Service;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.model.Facility;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacilityService {

    private final FacilityRepository facilityRepository;

    public FacilityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    public List<String> searchFacilities(String location, String sport, String time) {
        List<Facility> facilities = facilityRepository.findByCityAndSportType(location, sport);
        
        // Filtrar ACTIVE no service - LÓGICA DE NEGÓCIO
        return facilities.stream()
            .filter(f -> "ACTIVE".equals(f.getStatus()))
            .map(Facility::getName)
            .collect(Collectors.toList());
    }
}
