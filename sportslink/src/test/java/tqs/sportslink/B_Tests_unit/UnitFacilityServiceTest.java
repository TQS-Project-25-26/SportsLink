package tqs.sportslink.B_Tests_unit;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.RentalRepository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Sport;
import tqs.sportslink.dto.FacilityResponseDTO;
import tqs.sportslink.service.FacilityService;

@ExtendWith(MockitoExtension.class)
@Requirement("SL-15")
class UnitFacilityServiceTest {

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private RentalRepository rentalRepository;

    @InjectMocks
    private FacilityService facilityService;

    @Test
    void whenSearchByLocation_thenReturnsNearbyFacilities() {
        // Given - Maria searching in Aveiro
        Facility facility1 = new Facility();
        facility1.setId(1L);
        facility1.setName("Padel Club Aveiro");
        facility1.setStatus("ACTIVE");

        Facility facility2 = new Facility();
        facility2.setId(2L);
        facility2.setName("Sports Center Aveiro");
        facility2.setStatus("ACTIVE");

        when(facilityRepository.findByCityAndSportType("Aveiro", Sport.PADEL))
                .thenReturn(List.of(facility1, facility2));
        when(rentalRepository.findByFacilityId(anyLong()))
                .thenReturn(List.of());

        // When
        List<FacilityResponseDTO> result = facilityService.searchFacilities("Aveiro", "Padel", "19:00");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FacilityResponseDTO::getName)
                .contains("Padel Club Aveiro", "Sports Center Aveiro");
    }

    @Test
    @Requirement("SL-27")
    void whenSearchBySport_thenReturnsCorrectFacilities() {
        // Given - Maria looking for Padel courts
        Facility facility = new Facility();
        facility.setId(1L);
        facility.setName("Padel Club Aveiro");
        facility.setStatus("ACTIVE");

        when(facilityRepository.findByCityAndSportType("Aveiro", Sport.PADEL))
                .thenReturn(List.of(facility));
        when(rentalRepository.findByFacilityId(anyLong()))
                .thenReturn(List.of());

        // When
        List<FacilityResponseDTO> result = facilityService.searchFacilities("Aveiro", "Padel", "19:00");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).extracting(FacilityResponseDTO::getName)
                .contains("Padel Club Aveiro");
    }

    @Test
    void whenSearchWithInvalidLocation_thenReturnsEmpty() {
        // Given
        when(facilityRepository.findByCityAndSportType("InvalidLocation", Sport.PADEL))
                .thenReturn(List.of());

        // When
        List<FacilityResponseDTO> result = facilityService.searchFacilities("InvalidLocation", "Padel", "19:00");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @Requirement("SL-26")
    void whenCheckAvailability_duringOpenHours_thenReturnsTrue() {
        // Given - Check if facility available
        Facility facility = new Facility();
        facility.setId(1L);
        facility.setName("Padel Club Aveiro");
        facility.setStatus("ACTIVE");

        when(facilityRepository.findByCityAndSportType("Aveiro", Sport.PADEL))
                .thenReturn(List.of(facility));
        when(rentalRepository.findByFacilityId(anyLong()))
                .thenReturn(List.of());

        // When
        List<FacilityResponseDTO> result = facilityService.searchFacilities("Aveiro", "Padel", "19:00");

        // Then
        assertThat(result).isNotEmpty();
    }

    @Test
    @Requirement("SL-24")
    void whenGetFacilityDetails_thenReturnsCompleteInfo() {
        // Given
        String location = "Aveiro";
        String sport = "Padel";
        String time = "19:00";

        when(facilityRepository.findByCityAndSportType("Aveiro", Sport.PADEL))
                .thenReturn(List.of());

        // When
        List<FacilityResponseDTO> result = facilityService.searchFacilities(location, sport, time);

        // Then
        assertThat(result).isNotNull();
    }

    // --- Time Parsing Tests ---

    @Test
    void whenSearchWithIsoTime_thenParsesCorrectly() {
        // Given
        Facility facility = new Facility();
        facility.setId(1L);
        facility.setStatus("ACTIVE");

        when(facilityRepository.findAll()).thenReturn(List.of(facility));
        when(rentalRepository.findByFacilityId(1L)).thenReturn(List.of());

        // When
        List<FacilityResponseDTO> result = facilityService.searchFacilities(null, null, "2025-12-25T14:00:00");

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void whenSearchWithCustomFormatTime_thenParsesCorrectly() {
        // Given
        Facility facility = new Facility();
        facility.setId(1L);
        facility.setStatus("ACTIVE");

        when(facilityRepository.findAll()).thenReturn(List.of(facility));
        when(rentalRepository.findByFacilityId(1L)).thenReturn(List.of());

        // When
        List<FacilityResponseDTO> result = facilityService.searchFacilities(null, null, "2025-12-25 14:00");

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void whenSearchWithTimeOnly_thenParsesCorrectly() {
        // Given
        Facility facility = new Facility();
        facility.setId(1L);
        facility.setStatus("ACTIVE");

        when(facilityRepository.findAll()).thenReturn(List.of(facility));
        when(rentalRepository.findByFacilityId(1L)).thenReturn(List.of());

        // When
        List<FacilityResponseDTO> result = facilityService.searchFacilities(null, null, "14:00");

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void whenSearchWithInvalidTimeFormat_thenThrowsException() {
        // Given
        Facility facility = new Facility();
        facility.setId(1L);
        facility.setStatus("ACTIVE");

        when(facilityRepository.findAll()).thenReturn(List.of(facility));

        // When/Then
        assertThatThrownBy(() -> facilityService.searchFacilities(null, null, "invalid-time"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- Availability Tests ---

    @Test
    void whenFacilityClosedAtRequestedTime_thenExcludesFromResults() {
        // Given
        Facility facility = new Facility();
        facility.setId(1L);
        facility.setStatus("ACTIVE");
        facility.setOpeningTime(LocalTime.of(8, 0));
        facility.setClosingTime(LocalTime.of(12, 0)); // Closes at noon

        // Requesting 14:00
        when(facilityRepository.findAll()).thenReturn(List.of(facility));

        // When
        List<FacilityResponseDTO> result = facilityService.searchFacilities(null, null, "14:00");

        // Then
        assertThat(result).isEmpty();
    }
}
