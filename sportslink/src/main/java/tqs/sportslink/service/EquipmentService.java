package tqs.sportslink.service;

import org.springframework.stereotype.Service;
import tqs.sportslink.data.EquipmentRepository;
import tqs.sportslink.data.model.Equipment;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    public List<String> getEquipmentsByFacility(Long facilityId) {
        List<Equipment> equipments = equipmentRepository.findByFacilityId(facilityId);
        
        // Filtrar AVAILABLE no service - LÓGICA DE NEGÓCIO
        return equipments.stream()
            .filter(e -> "AVAILABLE".equals(e.getStatus()))
            .map(Equipment::getName)
            .collect(Collectors.toList());
    }
}
