package tqs.sportslink.B_Tests_unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.sportslink.service.EquipmentService;
import tqs.sportslink.data.EquipmentRepository;
import tqs.sportslink.data.model.Equipment;
import tqs.sportslink.dto.EquipmentResponseDTO;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnitEquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private EquipmentService equipmentService;

    @Test
    void whenGetAvailableEquipment_forFacility_thenReturnsEquipmentList() {
        // Given - Maria checking equipment for Padel Club
        Equipment eq1 = new Equipment();
        eq1.setName("Raquete Profissional");
        eq1.setStatus("AVAILABLE");
        
        Equipment eq2 = new Equipment();
        eq2.setName("Bola Wilson");
        eq2.setStatus("AVAILABLE");
        
        when(equipmentRepository.findByFacilityId(1L))
            .thenReturn(List.of(eq1, eq2));
        
        // When
        List<EquipmentResponseDTO> result = equipmentService.getEquipmentsByFacility(1L);
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(EquipmentResponseDTO::getName)
            .contains("Raquete Profissional", "Bola Wilson");
    }

    @Test
    void whenGetEquipments_thenIncludesRacketsAndBalls() {
        // Given
        Equipment eq1 = new Equipment();
        eq1.setName("Bola");
        eq1.setStatus("AVAILABLE");
        
        Equipment eq2 = new Equipment();
        eq2.setName("Raquete");
        eq2.setStatus("AVAILABLE");
        
        Equipment eq3 = new Equipment();
        eq3.setName("Rede");
        eq3.setStatus("AVAILABLE");
        
        when(equipmentRepository.findByFacilityId(1L))
            .thenReturn(List.of(eq1, eq2, eq3));
        
        // When
        List<EquipmentResponseDTO> result = equipmentService.getEquipmentsByFacility(1L);
        
        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(EquipmentResponseDTO::getName)
            .contains("Bola", "Raquete", "Rede");
    }



    @Test
    void whenGetEquipments_withUnavailableItems_thenExcludesUnavailable() {
        // Given
        Equipment eq1 = new Equipment();
        eq1.setName("Bola");
        eq1.setStatus("AVAILABLE");
        
        Equipment eq2 = new Equipment();
        eq2.setName("Raquete");
        eq2.setStatus("UNAVAILABLE");
        
        when(equipmentRepository.findByFacilityId(1L))
            .thenReturn(List.of(eq1, eq2));
        
        // When
        List<EquipmentResponseDTO> result = equipmentService.getEquipmentsByFacility(1L);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result).extracting(EquipmentResponseDTO::getName)
            .contains("Bola");
        assertThat(result).extracting(EquipmentResponseDTO::getName)
            .doesNotContain("Raquete");
    }
}
