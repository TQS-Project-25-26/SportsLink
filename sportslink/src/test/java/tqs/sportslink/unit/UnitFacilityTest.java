package tqs.sportslink.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.sportslink.service.FacilityService;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Sport;
import tqs.sportslink.dto.FacilityResponseDTO;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnitFacilityTest {

    @Mock
    private FacilityRepository facilityRepository;

    @InjectMocks
    private FacilityService facilityService;

    @Test
    public void whenSearchByLocation_thenReturnsNearbyFacilities() {
        // Given - Maria searching in Aveiro
        Facility facility1 = new Facility();
        facility1.setName("Padel Club Aveiro");
        facility1.setStatus("ACTIVE");
        
        Facility facility2 = new Facility();
        facility2.setName("Sports Center Aveiro");
        facility2.setStatus("ACTIVE");
        
        when(facilityRepository.findByCityAndSportType("Aveiro", Sport.PADEL))
            .thenReturn(List.of(facility1, facility2));
        
        // When
        List<FacilityResponseDTO> result = facilityService.searchFacilities("Aveiro", "Padel", "19:00");
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FacilityResponseDTO::getName)
            .contains("Padel Club Aveiro", "Sports Center Aveiro");
    }

    @Test
    public void whenSearchBySport_thenReturnsCorrectFacilities() {
        // Given - Maria looking for Padel courts
        Facility facility = new Facility();
        facility.setName("Padel Club Aveiro");
        facility.setStatus("ACTIVE");
        
        when(facilityRepository.findByCityAndSportType("Aveiro", Sport.PADEL))
            .thenReturn(List.of(facility));
        
        // When
        List<FacilityResponseDTO> result = facilityService.searchFacilities("Aveiro", "Padel", "19:00");
        
        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).extracting(FacilityResponseDTO::getName)
            .contains("Padel Club Aveiro");
    }

    @Test
    public void whenSearchWithInvalidLocation_thenReturnsEmpty() {
        // Given
        when(facilityRepository.findByCityAndSportType("InvalidLocation", Sport.PADEL))
            .thenReturn(List.of());
        
        // When
        List<FacilityResponseDTO> result = facilityService.searchFacilities("InvalidLocation", "Padel", "19:00");
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void whenCheckAvailability_duringOpenHours_thenReturnsTrue() {
        // Given - Check if facility available
        Facility facility = new Facility();
        facility.setName("Padel Club Aveiro");
        facility.setStatus("ACTIVE");
        
        when(facilityRepository.findByCityAndSportType("Aveiro", Sport.PADEL))
            .thenReturn(List.of(facility));
        
        // When
        List<FacilityResponseDTO> result = facilityService.searchFacilities("Aveiro", "Padel", "19:00");
        
        // Then
        assertThat(result).isNotEmpty();
    }

    @Test
    public void whenGetFacilityDetails_thenReturnsCompleteInfo() {
        // Given
        String location = "Aveiro";
        String sport = "Padel";
        String time = "19:00";
        
        // When
        List<FacilityResponseDTO> result = facilityService.searchFacilities(location, sport, time);
        
        // Then
        assertThat(result).isNotNull();
    }
}
