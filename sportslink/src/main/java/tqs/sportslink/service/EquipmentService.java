package tqs.sportslink.service;

import org.springframework.stereotype.Service;
import tqs.sportslink.data.EquipmentRepository;
import tqs.sportslink.data.model.Equipment;
import tqs.sportslink.dto.EquipmentResponseDTO;
import java.util.List;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    public List<EquipmentResponseDTO> getEquipmentsByFacility(Long facilityId) {
        List<Equipment> equipments = equipmentRepository.findByFacilityId(facilityId);
        
        // Filtrar AVAILABLE no service - LÓGICA DE NEGÓCIO
        return equipments.stream()
            .filter(e -> "AVAILABLE".equals(e.getStatus()))
            .map(this::toDTO)
            .toList();
    }
    
    private EquipmentResponseDTO toDTO(Equipment equipment) {
        return new EquipmentResponseDTO(
            equipment.getId(),
            equipment.getName(),
            equipment.getType(),
            equipment.getDescription(),
            equipment.getQuantity(),
            equipment.getPricePerHour(),
            equipment.getStatus()
        );
    }
}
