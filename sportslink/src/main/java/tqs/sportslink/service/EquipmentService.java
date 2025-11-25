package tqs.sportslink.service;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EquipmentService {

    public List<String> getEquipmentsByFacility(Long facilityId) {
        // Lógica para obter equipamentos
        return List.of("Bola", "Raquete", "Rede"); // Mock
    }
}
