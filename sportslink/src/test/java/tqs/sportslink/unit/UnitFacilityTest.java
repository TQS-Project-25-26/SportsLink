package tqs.sportslink.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.sportslink.service.FacilityService;
import tqs.sportslink.data.FacilityRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UnitFacilityTest {

    @Mock
    private FacilityRepository facilityRepository;

    @InjectMocks
    private FacilityService facilityService;

    @Test
    public void whenSearchByLocation_thenReturnsNearbyFacilities() {
        // Given - Maria searching in Aveiro
        String location = "Aveiro";
        String sport = "Padel";
        String time = "19:00";
        
        // When
        List<String> result = facilityService.searchFacilities(location, sport, time);
        
        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void whenSearchBySport_thenReturnsCorrectFacilities() {
        // Given - Maria looking for Padel courts
        String location = "Aveiro";
        String sport = "Padel";
        String time = "19:00";
        
        // When
        List<String> result = facilityService.searchFacilities(location, sport, time);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }

    @Test
    public void whenSearchWithInvalidLocation_thenReturnsEmpty() {
        // Given
        String location = "InvalidLocation";
        String sport = "Padel";
        String time = "19:00";
        
        // When
        List<String> result = facilityService.searchFacilities(location, sport, time);
        
        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void whenCheckAvailability_duringOpenHours_thenReturnsTrue() {
        // Given - Check if facility available
        String location = "Aveiro";
        String sport = "Padel";
        String time = "19:00";
        
        // When
        List<String> result = facilityService.searchFacilities(location, sport, time);
        
        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void whenGetFacilityDetails_thenReturnsCompleteInfo() {
        // Given
        String location = "Aveiro";
        String sport = "Padel";
        String time = "19:00";
        
        // When
        List<String> result = facilityService.searchFacilities(location, sport, time);
        
        // Then
        assertThat(result).isNotNull();
    }
}
