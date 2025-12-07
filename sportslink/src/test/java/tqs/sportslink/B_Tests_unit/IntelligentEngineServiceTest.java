package tqs.sportslink.B_Tests_unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.sportslink.data.EquipmentRepository;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.RentalRepository;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.*;
import tqs.sportslink.dto.EquipmentSuggestionDTO;
import tqs.sportslink.dto.FacilitySuggestionDTO;
import tqs.sportslink.dto.OwnerSuggestionDTO;
import tqs.sportslink.service.IntelligentEngineService;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntelligentEngineServiceTest {

    @Mock
    private FacilityRepository facilityRepository;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private EquipmentRepository equipmentRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private IntelligentEngineService service;

    private User user;
    private Facility facility;
    private Equipment football;
    private Equipment basketball;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.getRoles().add(Role.RENTER);
        user.setLatitude(40.0);
        user.setLongitude(-8.0);

        facility = new Facility();
        facility.setId(10L);
        facility.setName("City Sports - Visited");
        facility.setCity("Aveiro");
        facility.setSports(Arrays.asList(Sport.FOOTBALL, Sport.TENNIS));
        facility.setStatus("ACTIVE");
        facility.setRating(4.5);
        facility.setLatitude(40.01);
        facility.setLongitude(-8.01);
        facility.setUpdatedAt(LocalDateTime.now().minusDays(10)); // NPE Fix

        football = new Equipment();
        football.setId(100L);
        football.setName("Pro Football");
        football.setType("Ball");
        football.setStatus("AVAILABLE");
        football.setSports(Collections.singletonList(Sport.FOOTBALL));

        basketball = new Equipment();
        basketball.setId(101L);
        basketball.setName("Pro Basketball");
        basketball.setType("Ball");
        basketball.setStatus("AVAILABLE");
        basketball.setSports(Collections.singletonList(Sport.BASKETBALL));
    }

    @Test
    void shouldSuggestFacilitiesBasedOnCityAndSport() {
        // Arrange
        // Create a new facility that hasn't been visited but matches preferences
        Facility newFacility = new Facility();
        newFacility.setId(11L);
        newFacility.setName("New City Sports");
        newFacility.setCity("Aveiro"); // Matches preferred city
        newFacility.setSports(Arrays.asList(Sport.FOOTBALL)); // Matches preferred sport
        newFacility.setStatus("ACTIVE");
        newFacility.setRating(5.0);
        newFacility.setLatitude(40.02);
        newFacility.setLongitude(-8.02);
        newFacility.setUpdatedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Mock rentals to establish preference for Football in Aveiro (using the
        // 'facility' from setUp)
        Rental rental = new Rental();
        rental.setFacility(facility);
        when(rentalRepository.findByUserId(1L)).thenReturn(Collections.singletonList(rental));

        // Return both facilities
        when(facilityRepository.findAll()).thenReturn(Arrays.asList(facility, newFacility));

        // Act
        List<FacilitySuggestionDTO> suggestions = service.suggestFacilitiesForUser(1L);

        // Assert
        assertFalse(suggestions.isEmpty());
        FacilitySuggestionDTO suggestion = suggestions.get(0);
        assertEquals("New City Sports", suggestion.getName()); // Suggest the unvisited one
        assertTrue(suggestion.getScore() > 50.0); // High score expected
    }

    @Test
    void shouldSuggestEquipmentStrictlyForSport() {
        // Arrange
        when(equipmentRepository.findByFacilityId(10L)).thenReturn(Arrays.asList(football, basketball));

        // Act
        List<EquipmentSuggestionDTO> result = service.suggestEquipmentForSport(10L, "FOOTBALL");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Pro Football", result.get(0).getName());
        // Basketball should NOT be suggested even though it's a "Ball"
    }

    @Test
    void shouldNotSuggestEquipmentForMismatchingSport() {
        // Arrange
        when(equipmentRepository.findByFacilityId(10L)).thenReturn(Arrays.asList(football, basketball));

        // Act
        List<EquipmentSuggestionDTO> result = service.suggestEquipmentForSport(10L, "TENNIS");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSuggestImprovementsForOwner() {
        // Arrange
        User owner = new User();
        owner.setId(2L);
        owner.getRoles().add(Role.OWNER);
        owner.setFacilities(Collections.singletonList(facility));

        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));

        // Mock low rentals
        when(rentalRepository.findByFacilityId(10L)).thenReturn(Collections.emptyList());

        // Act
        List<OwnerSuggestionDTO> results = service.suggestImprovementsForOwner(2L);

        // Assert
        assertFalse(results.isEmpty());
        // Should suggest LOWER_PRICE because of low rentals
        assertEquals("LOWER_PRICE", results.get(0).getType());
    }
}
