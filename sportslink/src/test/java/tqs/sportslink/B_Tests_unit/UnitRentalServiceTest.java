package tqs.sportslink.B_Tests_unit;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import tqs.sportslink.data.EquipmentRepository;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.RentalRepository;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Equipment;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Rental;
import tqs.sportslink.data.model.User;
import tqs.sportslink.dto.RentalRequestDTO;
import tqs.sportslink.dto.RentalResponseDTO;
import tqs.sportslink.service.RentalService;

@ExtendWith(MockitoExtension.class)
public class UnitRentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RentalService rentalService;

    private RentalRequestDTO validRequest;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Facility mockFacility;
    private User mockUser;
    private Rental mockRental;

    @BeforeEach
    public void setUp() {
        startTime = LocalDateTime.now().plusDays(29).withHour(19).withMinute(0).withSecond(0).withNano(0);
        endTime = startTime.plusHours(2); // 2 hours later

        validRequest = new RentalRequestDTO();
        validRequest.setUserId(1L);
        validRequest.setFacilityId(1L);
        validRequest.setStartTime(startTime);
        validRequest.setEndTime(endTime);

        mockFacility = new Facility();
        mockFacility.setId(1L);
        mockFacility.setName("Padel Club Aveiro");
        mockFacility.setOpeningTime(LocalTime.of(8, 0));
        mockFacility.setClosingTime(LocalTime.of(22, 0));

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setName("Test User");

        mockRental = new Rental();
        mockRental.setId(1L);
        mockRental.setFacility(mockFacility);
        mockRental.setUser(mockUser);
        mockRental.setStartTime(startTime);
        mockRental.setEndTime(endTime);
        mockRental.setStatus("CONFIRMED");
    }

    @Test
    @Requirement("SL-26")
    void whenCreateRental_withValidRequest_thenSuccess() {
        when(rentalRepository.findByFacilityIdAndStartTimeLessThanAndEndTimeGreaterThan(
                anyLong(), any(), any())).thenReturn(List.of());
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(rentalRepository.save(any(Rental.class))).thenReturn(mockRental);

        RentalResponseDTO result = rentalService.createRental(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    @Requirement("SL-26")
    void whenCreateRental_withEquipment_thenSuccess() {
        validRequest.setEquipmentIds(List.of(1L, 2L));
        Equipment eq1 = new Equipment();
        eq1.setId(1L);
        eq1.setName("Raquete");
        eq1.setQuantity(10);
        Equipment eq2 = new Equipment();
        eq2.setId(2L);
        eq2.setName("Bola");
        eq2.setQuantity(10);

        when(rentalRepository.findByFacilityIdAndStartTimeLessThanAndEndTimeGreaterThan(
                anyLong(), any(), any())).thenReturn(List.of());
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(equipmentRepository.findAllById(anyList())).thenReturn(List.of(eq1, eq2));

        mockRental.setEquipments(List.of(eq1, eq2));
        when(rentalRepository.save(any(Rental.class))).thenReturn(mockRental);

        RentalResponseDTO result = rentalService.createRental(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getEquipments()).hasSize(2);
        verify(equipmentRepository).findAllById(anyList());
    }

    @Test
    @Requirement("SL-26")
    void whenCreateRental_facilityAlreadyBooked_shouldThrowException() {
        Rental conflicting = new Rental();
        conflicting.setStatus("CONFIRMED");
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility));
        when(rentalRepository.findByFacilityIdAndStartTimeLessThanAndEndTimeGreaterThan(
                anyLong(), any(), any())).thenReturn(List.of(conflicting));

        assertThatThrownBy(() -> rentalService.createRental(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    @Requirement("SL-26")
    void whenCreateRental_facilityNotFound_shouldThrowException() {
        when(facilityRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalService.createRental(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @Requirement("SL-26")
    void whenCancelRental_validId_thenSuccess() {
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));
        Rental cancelledRental = new Rental();
        cancelledRental.setId(1L);
        cancelledRental.setStatus("CANCELLED");
        cancelledRental.setFacility(mockFacility);
        cancelledRental.setUser(mockUser);
        cancelledRental.setStartTime(startTime);
        cancelledRental.setEndTime(endTime);
        when(rentalRepository.save(any(Rental.class))).thenReturn(cancelledRental);

        RentalResponseDTO result = rentalService.cancelRental(1L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    @Requirement("SL-26")
    void whenCancelRental_invalidId_shouldThrowException() {
        when(rentalRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalService.cancelRental(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @Requirement("SL-26")
    void whenUpdateRental_validChange_thenSuccess() {
        RentalRequestDTO updateRequest = new RentalRequestDTO();
        updateRequest.setUserId(1L);
        updateRequest.setFacilityId(1L);
        updateRequest
                .setStartTime(LocalDateTime.now().plusDays(31).withHour(20).withMinute(0).withSecond(0).withNano(0));
        updateRequest.setEndTime(LocalDateTime.now().plusDays(31).withHour(22).withMinute(0).withSecond(0).withNano(0));

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility)); // Mock facility requirement for update
        when(rentalRepository.findByFacilityIdAndStartTimeLessThanAndEndTimeGreaterThan(
                anyLong(), any(), any())).thenReturn(List.of(mockRental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(mockRental);

        RentalResponseDTO result = rentalService.updateRental(1L, updateRequest);

        assertThat(result).isNotNull();
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    @Requirement("SL-26")
    void whenGetRentalStatus_validId_thenReturnsStatus() {
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));

        RentalResponseDTO result = rentalService.getRentalStatus(1L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @Requirement("SL-28")
    void whenCreateRental_inPast_shouldThrowException() {
        validRequest.setStartTime(LocalDateTime.now().minusDays(1));
        validRequest.setEndTime(LocalDateTime.now().minusDays(1).plusHours(2));

        assertThatThrownBy(() -> rentalService.createRental(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot create rental in the past");
    }

    @Test
    @Requirement("SL-26")
    void whenCreateRental_endTimeBeforeStartTime_shouldThrowException() {
        validRequest.setStartTime(LocalDateTime.now().plusDays(1).withHour(21).withMinute(0));
        validRequest.setEndTime(LocalDateTime.now().plusDays(1).withHour(19).withMinute(0));

        assertThatThrownBy(() -> rentalService.createRental(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End time must be after start time");
    }

    @Test
    @Requirement("SL-26")
    void whenCreateRental_durationTooShort_shouldThrowException() {
        validRequest.setStartTime(LocalDateTime.now().plusDays(1).withHour(19).withMinute(0).withSecond(0).withNano(0));
        validRequest.setEndTime(LocalDateTime.now().plusDays(1).withHour(19).withMinute(30).withSecond(0).withNano(0));

        assertThatThrownBy(() -> rentalService.createRental(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rental duration must be at least 1 hour");
    }

    @Test
    @Requirement("SL-26")
    void whenUpdateRental_toPastTime_shouldThrowException() {
        RentalRequestDTO updateRequest = new RentalRequestDTO();
        updateRequest.setUserId(1L);
        updateRequest.setFacilityId(1L);
        updateRequest.setStartTime(LocalDateTime.now().minusDays(1));
        updateRequest.setEndTime(LocalDateTime.now().minusDays(1).plusHours(2));

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));

        assertThatThrownBy(() -> rentalService.updateRental(1L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot update rental to past time");
    }

    @Test
    @Requirement("SL-26")
    void whenUpdateRental_toTimeLessThan24HoursAhead_shouldThrowException() {
        RentalRequestDTO updateRequest = new RentalRequestDTO();
        updateRequest.setUserId(1L);
        updateRequest.setFacilityId(1L);
        // 2 hours from now (less than 24h)
        updateRequest.setStartTime(LocalDateTime.now().plusHours(2));
        updateRequest.setEndTime(LocalDateTime.now().plusHours(4));

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));

        assertThatThrownBy(() -> rentalService.updateRental(1L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Updates must be made for a time at least 24 hours in the future");
    }

    @Test
    @Requirement("SL-26")
    void whenUpdateRental_withValidTimeLimit_thenSuccess() {
        RentalRequestDTO updateRequest = new RentalRequestDTO();
        updateRequest.setUserId(1L);
        updateRequest.setFacilityId(1L);
        // 25 hours from now (valid), ensuring it falls within operating hours (08:00 - 22:00)
        // Using plusDays(2).withHour(14) makes it 2PM the day after tomorrow, which is safe (> 24h) and within hours.
        LocalDateTime validStart = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0).withSecond(0).withNano(0);
        
        updateRequest.setStartTime(validStart);
        updateRequest.setEndTime(validStart.plusHours(2));

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility)); // Mock facility requirement for update
        when(rentalRepository.findByFacilityIdAndStartTimeLessThanAndEndTimeGreaterThan(
                anyLong(), any(), any())).thenReturn(List.of(mockRental)); // Self conflict ignored in service
        when(rentalRepository.save(any(Rental.class))).thenReturn(mockRental);

        RentalResponseDTO result = rentalService.updateRental(1L, updateRequest);

        assertThat(result).isNotNull();
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    @Requirement("SL-29")
    void whenUpdateRental_outsideFacilityHours_shouldThrowException() {
        RentalRequestDTO updateRequest = new RentalRequestDTO();
        updateRequest.setUserId(1L);
        updateRequest.setFacilityId(1L);
        // Valid future time (25h+) but outside operating hours (e.g. 02:00 AM)
        LocalDateTime invalidTime = LocalDateTime.now().plusDays(2).withHour(2).withMinute(0);
        updateRequest.setStartTime(invalidTime);
        updateRequest.setEndTime(invalidTime.plusHours(2));

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility)); // Mock facility return

        assertThatThrownBy(() -> rentalService.updateRental(1L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outside facility operating hours");
    }

    @Test
    @Requirement("SL-29")
    void whenUpdateRental_conflictsWithExistingBooking_shouldThrowException() {
        RentalRequestDTO updateRequest = new RentalRequestDTO();
        updateRequest.setUserId(1L);
        updateRequest.setFacilityId(1L);
        
        LocalDateTime validStart = LocalDateTime.now().plusHours(30).withHour(15).withMinute(0);
        updateRequest.setStartTime(validStart);
        updateRequest.setEndTime(validStart.plusHours(1));

        Rental conflicting = new Rental();
        conflicting.setId(2L); // Different ID
        conflicting.setStatus("CONFIRMED");

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility));
        when(rentalRepository.findByFacilityIdAndStartTimeLessThanAndEndTimeGreaterThan(
                anyLong(), any(), any())).thenReturn(List.of(conflicting));

        assertThatThrownBy(() -> rentalService.updateRental(1L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("conflicts with existing booking");
    }

    @Test
    @Requirement("SL-30")
    void whenCancelRental_alreadyCancelled_shouldThrowException() {
        mockRental.setStatus("CANCELLED");
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));

        assertThatThrownBy(() -> rentalService.cancelRental(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    @Requirement("SL-26")
    void whenCreateRental_outsideFacilityHours_shouldThrowException() {
        RentalRequestDTO invalidRequest = new RentalRequestDTO();
        invalidRequest.setUserId(1L);
        invalidRequest.setFacilityId(1L);
        invalidRequest
                .setStartTime(LocalDateTime.now().plusDays(1).withHour(7).withMinute(0).withSecond(0).withNano(0));
        invalidRequest.setEndTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0));
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility));

        assertThatThrownBy(() -> rentalService.createRental(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outside facility operating hours");
    }

    @Test
    @Requirement("SL-26")
    void whenCreateRental_durationTooLong_shouldThrowException() {
        validRequest.setStartTime(LocalDateTime.now().plusDays(1).withHour(18).withMinute(0).withSecond(0).withNano(0));
        validRequest.setEndTime(LocalDateTime.now().plusDays(1).withHour(23).withMinute(0).withSecond(0).withNano(0));

        assertThatThrownBy(() -> rentalService.createRental(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rental duration cannot exceed 4 hours");
    }

    @Test
    @Requirement("SL-26")
    void whenCreateRental_tooCloseToStartTime_shouldThrowException() {
        validRequest.setStartTime(LocalDateTime.now().plusMinutes(30));
        validRequest.setEndTime(LocalDateTime.now().plusMinutes(90));

        assertThatThrownBy(() -> rentalService.createRental(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rental must be booked at least 1 hour in advance");
    }

    @Test
    @Requirement("SL-28")
    void whenCreateRental_tooFarInFuture_shouldThrowException() {
        // More than 30 days
        validRequest
                .setStartTime(LocalDateTime.now().plusDays(31).withHour(19).withMinute(0).withSecond(0).withNano(0));
        validRequest.setEndTime(LocalDateTime.now().plusDays(31).withHour(21).withMinute(0).withSecond(0).withNano(0));

        assertThatThrownBy(() -> rentalService.createRental(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rental cannot be booked more than 30 days in advance");
    }

    @Test
    @Requirement("SL-29")
    void whenCreateRental_endTimeAfterClosingTime_shouldThrowException() {
        RentalRequestDTO invalidRequest = new RentalRequestDTO();
        invalidRequest.setUserId(1L);
        invalidRequest.setFacilityId(1L);
        invalidRequest
                .setStartTime(LocalDateTime.now().plusDays(1).withHour(20).withMinute(0).withSecond(0).withNano(0));
        invalidRequest.setEndTime(LocalDateTime.now().plusDays(1).withHour(23).withMinute(0).withSecond(0).withNano(0));

        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility));

        assertThatThrownBy(() -> rentalService.createRental(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outside facility operating hours");
    }

    @Test
    @Requirement("SL-29")
    void whenCancelRental_alreadyPassed_shouldThrowException() {
        mockRental.setStartTime(LocalDateTime.now().minusDays(2));
        mockRental.setEndTime(LocalDateTime.now().minusDays(2).plusHours(2));
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));

        assertThatThrownBy(() -> rentalService.cancelRental(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot cancel rental that has already passed");
    }

    @Test
    @Requirement("SL-26")
    void whenCreateRental_withEquipment_shouldReduceStock() {
        validRequest.setEquipmentIds(List.of(1L));
        Equipment eq1 = new Equipment();
        eq1.setId(1L);
        eq1.setName("Raquete");
        eq1.setQuantity(5);

        when(rentalRepository.findByFacilityIdAndStartTimeLessThanAndEndTimeGreaterThan(
                anyLong(), any(), any())).thenReturn(List.of());
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(equipmentRepository.findAllById(anyList())).thenReturn(List.of(eq1));
        when(rentalRepository.save(any(Rental.class))).thenReturn(mockRental);

        RentalResponseDTO result = rentalService.createRental(validRequest);

        assertThat(result).isNotNull();
        assertThat(eq1.getQuantity()).isEqualTo(4);
        verify(equipmentRepository).saveAll(anyList());
    }

    @Test
    @Requirement("SL-26")
    void whenCreateRental_equipmentOutOfStock_shouldThrowException() {
        validRequest.setEquipmentIds(List.of(1L));
        Equipment eq1 = new Equipment();
        eq1.setId(1L);
        eq1.setName("Raquete");
        eq1.setQuantity(0);

        when(rentalRepository.findByFacilityIdAndStartTimeLessThanAndEndTimeGreaterThan(
                anyLong(), any(), any())).thenReturn(List.of());
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(equipmentRepository.findAllById(anyList())).thenReturn(List.of(eq1));

        assertThatThrownBy(() -> rentalService.createRental(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("out of stock");
    }

    @Test
    @Requirement("SL-82")
    void whenUpdateRental_withEquipmentChange_shouldUpdateStock() {
        // Setup existing rental with equipment Racket (Pool: 5 -> 4 rented)
        Equipment oldEq = new Equipment();
        oldEq.setId(1L);
        oldEq.setName("Raquete");
        oldEq.setQuantity(4); // Current stock
        mockRental.setEquipments(List.of(oldEq));

        // Setup Request with new equipment Ball (Pool: 10)
        RentalRequestDTO updateRequest = new RentalRequestDTO();
        updateRequest.setUserId(1L);
        updateRequest.setFacilityId(1L);
        updateRequest.setStartTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0).withSecond(0).withNano(0));
        updateRequest.setEndTime(updateRequest.getStartTime().plusHours(2));
        updateRequest.setEquipmentIds(List.of(2L)); // Requesting Ball

        Equipment newEq = new Equipment();
        newEq.setId(2L);
        newEq.setName("Bola");
        newEq.setQuantity(10); // Current stock

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility));
        when(rentalRepository.findByFacilityIdAndStartTimeLessThanAndEndTimeGreaterThan(anyLong(), any(), any())).thenReturn(List.of(mockRental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(mockRental);
        when(equipmentRepository.findAllById(List.of(2L))).thenReturn(List.of(newEq));

        RentalResponseDTO result = rentalService.updateRental(1L, updateRequest);

        assertThat(result).isNotNull();
        // Verify old equipment stock increased (returned)
        assertThat(oldEq.getQuantity()).isEqualTo(5);
        // Verify new equipment stock decreased (rented)
        assertThat(newEq.getQuantity()).isEqualTo(9);
        
        verify(equipmentRepository).saveAll(List.of(oldEq)); // Returned
        verify(equipmentRepository).saveAll(List.of(newEq)); // Rented
    }

    @Test
    @Requirement("SL-82")
    void whenUpdateRental_newEquipmentOutOfStock_shouldThrowException() {
        mockRental.setEquipments(List.of()); // No old equipment

        // Setup Request with new equipment Ball (Pool: 0)
        RentalRequestDTO updateRequest = new RentalRequestDTO();
        updateRequest.setUserId(1L);
        updateRequest.setFacilityId(1L);
        updateRequest.setStartTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0).withSecond(0).withNano(0));
        updateRequest.setEndTime(updateRequest.getStartTime().plusHours(2));
        updateRequest.setEquipmentIds(List.of(2L)); // Requesting Ball

        Equipment newEq = new Equipment();
        newEq.setId(2L);
        newEq.setName("Bola");
        newEq.setQuantity(0); // Out of stock

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility));
        when(rentalRepository.findByFacilityIdAndStartTimeLessThanAndEndTimeGreaterThan(anyLong(), any(), any())).thenReturn(List.of(mockRental));
        when(equipmentRepository.findAllById(List.of(2L))).thenReturn(List.of(newEq));

        assertThatThrownBy(() -> rentalService.updateRental(1L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("out of stock");
    }
}