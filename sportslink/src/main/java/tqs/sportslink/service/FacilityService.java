package tqs.sportslink.service;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FacilityService {

    public List<String> searchFacilities(String location, String sport, String time) {
        // Lógica de busca mock - integrar com repositório depois
        return List.of("Facility 1", "Facility 2");
    }
}
