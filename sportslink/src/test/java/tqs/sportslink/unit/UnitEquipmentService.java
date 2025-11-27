package tqs.sportslink.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.sportslink.service.EquipmentService;
import tqs.sportslink.data.EquipmentRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UnitEquipmentService {

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private EquipmentService equipmentService;

    @Test
    public void whenGetAvailableEquipment_forFacility_thenReturnsEquipmentList() {
        // Given - Maria checking equipment for Padel Club
        Long facilityId = 1L;
        
        // When
        List<String> result = equipmentService.getEquipmentsByFacility(facilityId);
        
        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void whenGetEquipments_thenIncludesRacketsAndBalls() {
        // Given
        Long facilityId = 1L;
        
        // When
        List<String> result = equipmentService.getEquipmentsByFacility(facilityId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("Bola", "Raquete", "Rede");
    }
}
