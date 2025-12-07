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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        facility.setPricePerHour(20.0);
        facility.setLatitude(40.01);
        facility.setLongitude(-8.01);
        facility.setUpdatedAt(LocalDateTime.now().minusDays(10));

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

    // --- Facility Suggestion Tests ---

    @Test
    void whenUserHasNoRentals_thenSuggestTopRatedFacilities() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(rentalRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        Facility topRated = new Facility();
        topRated.setId(99L);
        topRated.setName("Elite Gym");
        topRated.setStatus("ACTIVE");
        topRated.setRating(5.0);

        Facility averageGym = new Facility();
        averageGym.setId(98L);
        averageGym.setName("Average Gym");
        averageGym.setStatus("ACTIVE");
        averageGym.setRating(3.0);

        when(facilityRepository.findAll()).thenReturn(List.of(topRated, averageGym));

        // Act
        List<FacilitySuggestionDTO> suggestions = service.suggestFacilitiesForUser(1L);

        // Assert
        assertThat(suggestions).hasSize(1); // Only > 4.0 are top rated
        assertThat(suggestions.get(0).getName()).isEqualTo("Elite Gym");
        assertThat(suggestions.get(0).getReason()).contains("Top rated");
    }

    @Test
    void whenUserHasCoordinates_thenPrioritizeProximity() {
        // Arrange
        // User at 40.0, -8.0
        Facility near = new Facility();
        near.setId(1L);
        near.setName("Near");
        near.setStatus("ACTIVE");
        near.setRating(4.0);
        near.setLatitude(40.01);
        near.setLongitude(-8.01); // Very close (~1.5km)
        near.setSports(List.of(Sport.PADEL));

        Facility far = new Facility();
        far.setId(2L);
        far.setName("Far");
        far.setStatus("ACTIVE");
        far.setRating(4.0);
        far.setLatitude(41.0); // ~100km away
        far.setLongitude(-8.0);
        far.setSports(List.of(Sport.PADEL));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Rental rental = new Rental();
        rental.setFacility(facility); // facility is in Aveiro
        when(rentalRepository.findByUserId(1L)).thenReturn(List.of(rental));

        when(facilityRepository.findAll()).thenReturn(List.of(near, far));

        // Act
        List<FacilitySuggestionDTO> suggestions = service.suggestFacilitiesForUser(1L);

        // Assert
        FacilitySuggestionDTO nearSugg = suggestions.stream().filter(s -> s.getName().equals("Near")).findFirst()
                .orElseThrow();
        FacilitySuggestionDTO farSugg = suggestions.stream().filter(s -> s.getName().equals("Far")).findFirst()
                .orElseThrow();

        assertThat(nearSugg.getScore()).isGreaterThan(farSugg.getScore());
        assertThat(nearSugg.getReason()).contains("km away");
    }

    @Test
    void whenUserHasNoCoordinates_thenPrioritizeCityMatch() {
        // Arrange
        user.setLatitude(null);
        user.setLongitude(null);

        Facility inCity = new Facility();
        inCity.setId(1L);
        inCity.setName("In City");
        inCity.setCity("Aveiro"); // Matches user preference
        inCity.setStatus("ACTIVE");
        inCity.setRating(4.0);
        inCity.setSports(List.of(Sport.FOOTBALL));

        Facility outCity = new Facility();
        outCity.setId(2L);
        outCity.setName("Out City");
        outCity.setCity("Lisbon");
        outCity.setStatus("ACTIVE");
        outCity.setRating(4.0);
        outCity.setSports(List.of(Sport.FOOTBALL));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Rental rental = new Rental();
        rental.setFacility(facility); // Aveiro
        when(rentalRepository.findByUserId(1L)).thenReturn(List.of(rental));

        when(facilityRepository.findAll()).thenReturn(List.of(inCity, outCity));

        // Act
        List<FacilitySuggestionDTO> suggestions = service.suggestFacilitiesForUser(1L);

        // Assert
        FacilitySuggestionDTO inCitySugg = suggestions.stream().filter(s -> s.getName().equals("In City")).findFirst()
                .orElseThrow();
        FacilitySuggestionDTO outCitySugg = suggestions.stream().filter(s -> s.getName().equals("Out City")).findFirst()
                .orElseThrow();

        assertThat(inCitySugg.getScore()).isGreaterThan(outCitySugg.getScore());
        assertThat(inCitySugg.getReason()).contains("In Aveiro");
    }

    @Test
    void whenSuggestingFacilities_thenExcludeVisitedOnes() {
        // Arrange
        // 'facility' (ID 10) is already in visited list
        Facility unvisited = new Facility();
        unvisited.setId(11L);
        unvisited.setName("Unvisited");
        unvisited.setStatus("ACTIVE");
        unvisited.setCity("Aveiro");
        unvisited.setSports(List.of(Sport.FOOTBALL));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Rental rental = new Rental();
        rental.setFacility(facility);
        when(rentalRepository.findByUserId(1L)).thenReturn(List.of(rental)); // Visited ID 10

        when(facilityRepository.findAll()).thenReturn(List.of(facility, unvisited));

        // Act
        List<FacilitySuggestionDTO> suggestions = service.suggestFacilitiesForUser(1L);

        // Assert
        assertThat(suggestions)
                .extracting(FacilitySuggestionDTO::getName)
                .contains("Unvisited")
                .doesNotContain("City Sports - Visited");
    }

    // --- Owner Suggestion Tests ---

    @Test
    void suggestImprovementsForOwner_whenHighDemand_SuggestAddEquipment() {
        // Arrange
        User owner = new User();
        owner.setId(2L);
        owner.getRoles().add(Role.OWNER);
        owner.setFacilities(List.of(facility));

        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));

        // Mock 21 rentals in last 30 days
        List<Rental> rentals = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            Rental r = new Rental();
            r.setCreatedAt(LocalDateTime.now().minusDays(1)); // Recent
            rentals.add(r);
        }
        when(rentalRepository.findByFacilityId(facility.getId())).thenReturn(rentals);

        // Mock Low Equipment Stock (< 5)
        when(equipmentRepository.findByFacilityId(facility.getId())).thenReturn(List.of(football)); // Size 1

        // Act
        List<OwnerSuggestionDTO> results = service.suggestImprovementsForOwner(2L);

        // Assert
        assertThat(results).isNotEmpty();
        OwnerSuggestionDTO suggestion = results.get(0);
        assertThat(suggestion.getType()).isEqualTo("ADD_EQUIPMENT");
        assertThat(suggestion.getPriority()).isEqualTo("HIGH");
    }

    @Test
    void suggestImprovementsForOwner_whenLowRating_SuggestMaintenance() {
        // Arrange
        User owner = new User();
        owner.setId(2L);
        owner.getRoles().add(Role.OWNER);
        facility.setRating(3.5); // Low Rating
        owner.setFacilities(List.of(facility));

        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(rentalRepository.findByFacilityId(facility.getId())).thenReturn(Collections.emptyList()); // Low rentals ->
                                                                                                       // Lower Price
                                                                                                       // logic
        // But also Low Rating -> Maintenance logic
        // The service adds multiple suggestions if conditions met

        // Act
        List<OwnerSuggestionDTO> results = service.suggestImprovementsForOwner(2L);

        // Assert
        assertThat(results).extracting(OwnerSuggestionDTO::getType)
                .contains("LOWER_PRICE", "MAINTENANCE");
        // EXPECTED: Low rentals (<5) triggers LOWER_PRICE. Low rating triggers
        // MAINTENANCE.
    }

    @Test
    void suggestImprovementsForOwner_whenFacilityOutdated_SuggestMaintenance() {
        // Arrange
        User owner = new User();
        owner.setId(2L);
        owner.getRoles().add(Role.OWNER);

        facility.setUpdatedAt(LocalDateTime.now().minusDays(100)); // Outdated > 90 days
        facility.setRating(4.5); // Good rating, but old
        owner.setFacilities(List.of(facility));

        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(rentalRepository.findByFacilityId(facility.getId())).thenReturn(Collections.emptyList());

        // Act
        List<OwnerSuggestionDTO> results = service.suggestImprovementsForOwner(2L);

        // Assert
        assertThat(results).extracting(OwnerSuggestionDTO::getType)
                .contains("MAINTENANCE");
    }

    @Test
    void suggestImprovementsForOwner_whenUserNotOwner_ThrowException() {
        // Arrange
        User notOwner = new User();
        notOwner.setId(3L);
        notOwner.getRoles().add(Role.RENTER); // No OWNER role

        when(userRepository.findById(3L)).thenReturn(Optional.of(notOwner));

        // Act & Assert
        assertThatThrownBy(() -> service.suggestImprovementsForOwner(3L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User is not an owner");
    }

    // --- Equipment Suggestion Tests ---

    @Test
    void suggestEquipmentForSport_whenValidSport_ReturnsMatches() {
        // Arrange
        when(equipmentRepository.findByFacilityId(10L)).thenReturn(Arrays.asList(football, basketball));

        // Act
        List<EquipmentSuggestionDTO> result = service.suggestEquipmentForSport(10L, "FOOTBALL");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Pro Football");
    }

    @Test
    void suggestEquipmentForSport_whenInvalidSport_ReturnsEmpty() {
        // Act
        List<EquipmentSuggestionDTO> result = service.suggestEquipmentForSport(10L, "NOT_A_SPORT");

        // Assert
        assertThat(result).isEmpty();
    }
}
