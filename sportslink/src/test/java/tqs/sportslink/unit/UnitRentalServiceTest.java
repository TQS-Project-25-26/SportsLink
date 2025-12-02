package tqs.sportslink.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.sportslink.service.RentalService;
import tqs.sportslink.data.RentalRepository;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.EquipmentRepository;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Rental;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Equipment;
import tqs.sportslink.data.model.User;
import tqs.sportslink.dto.RentalRequestDTO;
import tqs.sportslink.dto.RentalResponseDTO;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import app.getxray.xray.junit.customjunitxml.annotations.XrayTest;

@XrayTest(key = "SL-30")
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
        // Usar datas FUTURAS (29 dias à frente - dentro do limite de 30 dias)
        startTime = LocalDateTime.now().plusDays(29).withHour(19).withMinute(0).withSecond(0).withNano(0);
        endTime = startTime.plusHours(2); // 2 horas depois
        
        validRequest = new RentalRequestDTO();
        validRequest.setUserId(1L);
        validRequest.setFacilityId(1L);
        validRequest.setStartTime(startTime);
        validRequest.setEndTime(endTime);
        
        // Mock facility
        mockFacility = new Facility();
        mockFacility.setId(1L);
        mockFacility.setName("Padel Club Aveiro");
        mockFacility.setOpeningTime(LocalTime.of(8, 0));
        mockFacility.setClosingTime(LocalTime.of(22, 0));

        
        // Mock user
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setName("Test User");
        
        // Mock rental
        mockRental = new Rental();
        mockRental.setId(1L);
        mockRental.setFacility(mockFacility);
        mockRental.setUser(mockUser);
        mockRental.setStartTime(startTime);
        mockRental.setEndTime(endTime);
        mockRental.setStatus("CONFIRMED");
    }

    @Test
    public void whenCreateRental_withValidRequest_thenSuccess() {
        // Given
        when(rentalRepository.findByFacilityIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                anyLong(), any(), any())).thenReturn(List.of()); // Sem conflitos
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(rentalRepository.save(any(Rental.class))).thenReturn(mockRental);
        
        // When
        RentalResponseDTO result = rentalService.createRental(validRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    public void whenCreateRental_withEquipment_thenSuccess() {
        // Given
        validRequest.setEquipmentIds(List.of(1L, 2L));
        Equipment eq1 = new Equipment();
        eq1.setId(1L);
        eq1.setName("Raquete");
        Equipment eq2 = new Equipment();
        eq2.setId(2L);
        eq2.setName("Bola");
        
        when(rentalRepository.findByFacilityIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                anyLong(), any(), any())).thenReturn(List.of());
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(equipmentRepository.findAllById(anyList())).thenReturn(List.of(eq1, eq2));
        
        mockRental.setEquipments(List.of(eq1, eq2));
        when(rentalRepository.save(any(Rental.class))).thenReturn(mockRental);
        
        // When
        RentalResponseDTO result = rentalService.createRental(validRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEquipments()).hasSize(2);
        verify(equipmentRepository).findAllById(anyList());
    }

    @Test
    public void whenCreateRental_facilityAlreadyBooked_shouldThrowException() {
        // Given - Facility já tem reserva
        Rental conflicting = new Rental();
        conflicting.setStatus("CONFIRMED");
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility));
        when(rentalRepository.findByFacilityIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                anyLong(), any(), any())).thenReturn(List.of(conflicting));
        
        // When/Then
        assertThatThrownBy(() -> rentalService.createRental(validRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already booked");
    }

    @Test
    public void whenCreateRental_facilityNotFound_shouldThrowException() {
        // Given
        when(facilityRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> rentalService.createRental(validRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not found");
    }

    @Test
    public void whenCancelRental_validId_thenSuccess() {
        // Given
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));
        Rental cancelledRental = new Rental();
        cancelledRental.setId(1L);
        cancelledRental.setStatus("CANCELLED");
        cancelledRental.setFacility(mockFacility);
        cancelledRental.setUser(mockUser);
        cancelledRental.setStartTime(startTime);
        cancelledRental.setEndTime(endTime);
        when(rentalRepository.save(any(Rental.class))).thenReturn(cancelledRental);
        
        // When
        RentalResponseDTO result = rentalService.cancelRental(1L);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    public void whenCancelRental_invalidId_shouldThrowException() {
        // Given
        when(rentalRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> rentalService.cancelRental(999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not found");
    }

    @Test
    public void whenUpdateRental_validChange_thenSuccess() {
        // Given
        RentalRequestDTO updateRequest = new RentalRequestDTO();
        updateRequest.setUserId(1L);
        updateRequest.setFacilityId(1L);
        updateRequest.setStartTime(LocalDateTime.now().plusDays(31).withHour(20).withMinute(0).withSecond(0).withNano(0));
        updateRequest.setEndTime(LocalDateTime.now().plusDays(31).withHour(22).withMinute(0).withSecond(0).withNano(0));
        
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));
        when(rentalRepository.findByFacilityIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                anyLong(), any(), any())).thenReturn(List.of(mockRental)); // Apenas ele mesmo
        when(rentalRepository.save(any(Rental.class))).thenReturn(mockRental);
        
        // When
        RentalResponseDTO result = rentalService.updateRental(1L, updateRequest);
        
        // Then
        assertThat(result).isNotNull();
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    public void whenGetRentalStatus_validId_thenReturnsStatus() {
        // Given
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));
        
        // When
        RentalResponseDTO result = rentalService.getRentalStatus(1L);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    public void whenCreateRental_inPast_shouldThrowException() {
        // Given - Tentar criar rental no passado
        validRequest.setStartTime(LocalDateTime.now().minusDays(1));
        validRequest.setEndTime(LocalDateTime.now().minusDays(1).plusHours(2));
        
        // When/Then
        assertThatThrownBy(() -> rentalService.createRental(validRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot create rental in the past");
    }

    @Test
    public void whenCreateRental_endTimeBeforeStartTime_shouldThrowException() {
        // Given
        validRequest.setStartTime(LocalDateTime.now().plusDays(1).withHour(21).withMinute(0));
        validRequest.setEndTime(LocalDateTime.now().plusDays(1).withHour(19).withMinute(0));
        
        // When/Then
        assertThatThrownBy(() -> rentalService.createRental(validRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("End time must be after start time");
    }

    @Test
    public void whenCreateRental_durationTooShort_shouldThrowException() {
        // Given - Duração menor que 1 hora
        validRequest.setStartTime(LocalDateTime.now().plusDays(1).withHour(19).withMinute(0).withSecond(0).withNano(0));
        validRequest.setEndTime(LocalDateTime.now().plusDays(1).withHour(19).withMinute(30).withSecond(0).withNano(0));
        
        // When/Then
        assertThatThrownBy(() -> rentalService.createRental(validRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Rental duration must be at least 1 hour");
    }

    @Test
    public void whenUpdateRental_toPastTime_shouldThrowException() {
        // Given
        RentalRequestDTO updateRequest = new RentalRequestDTO();
        updateRequest.setUserId(1L);
        updateRequest.setFacilityId(1L);
        updateRequest.setStartTime(LocalDateTime.now().minusDays(1));
        updateRequest.setEndTime(LocalDateTime.now().minusDays(1).plusHours(2));
        
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));
        
        // When/Then
        assertThatThrownBy(() -> rentalService.updateRental(1L, updateRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot update rental to past time");
    }

    // Testes adicionais a considerar:
    // - Conflitos de equipamento
    // - Duração máxima de rental
    // - Facilities devem ter horários e rentals só devem ser feitas dentro desses horários
    // - Mínimo de tempo antes de marcar (1h antes)
    // - Máximo de tempo até quando se pode marcar (30 dias)
    // - Verificar se já há uma rental a essas horas (nao pareceu q estivesse a ser feito)
    // - Cancelar rental já passada/cancelada

    @Test
    public void whenCancelRental_alreadyCancelled_shouldThrowException() {
        // Given
        mockRental.setStatus("CANCELLED");
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));    
        // When/Then
        assertThatThrownBy(() -> rentalService.cancelRental(1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already cancelled");
    }

    @Test
    public void whenCreateRental_outsideFacilityHours_shouldThrowException() {
        // Given - Horário fora do horário de funcionamento da facility
        
        RentalRequestDTO invalidRequest = new RentalRequestDTO();
        invalidRequest.setUserId(1L);
        invalidRequest.setFacilityId(1L);
        invalidRequest.setStartTime(LocalDateTime.now().plusDays(1).withHour(7).withMinute(0).withSecond(0).withNano(0)); // Antes das 8h
        invalidRequest.setEndTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0));     
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility));    
        
        // When/Then
        assertThatThrownBy(() -> rentalService.createRental(invalidRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("outside facility operating hours");
    }

    @Test
    public void whenCreateRental_durationTooLong_shouldThrowException() {
        // Given - Duração maior que 4 horas
        validRequest.setStartTime(LocalDateTime.now().plusDays(1).withHour(18).withMinute(0).withSecond(0).withNano(0));
        validRequest.setEndTime(LocalDateTime.now().plusDays(1).withHour(23).withMinute(0).withSecond(0).withNano(0)); // 5 horas
        
        // When/Then
        assertThatThrownBy(() -> rentalService.createRental(validRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Rental duration cannot exceed 4 hours");
    }

    @Test
    public void whenCreateRental_tooCloseToStartTime_shouldThrowException() {
        // Given - Tentar reservar com menos de 1 hora de antecedência
        validRequest.setStartTime(LocalDateTime.now().plusMinutes(30));
        validRequest.setEndTime(LocalDateTime.now().plusMinutes(90));
        
        // When/Then
        assertThatThrownBy(() -> rentalService.createRental(validRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Rental must be booked at least 1 hour in advance");
    }

    @Test
    public void whenCreateRental_tooFarInFuture_shouldThrowException() {
        // Given - Tentar reservar com mais de 30 dias de antecedência
        validRequest.setStartTime(LocalDateTime.now().plusDays(31).withHour(19).withMinute(0).withSecond(0).withNano(0));
        validRequest.setEndTime(LocalDateTime.now().plusDays(31).withHour(21).withMinute(0).withSecond(0).withNano(0));
        
        // When/Then
        assertThatThrownBy(() -> rentalService.createRental(validRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Rental cannot be booked more than 30 days in advance");
    }

    @Test
    public void whenCreateRental_endTimeAfterClosingTime_shouldThrowException() {
        // Given - Termina depois do horário de fecho (22:00)
        RentalRequestDTO invalidRequest = new RentalRequestDTO();
        invalidRequest.setUserId(1L);
        invalidRequest.setFacilityId(1L);
        invalidRequest.setStartTime(LocalDateTime.now().plusDays(1).withHour(20).withMinute(0).withSecond(0).withNano(0));
        invalidRequest.setEndTime(LocalDateTime.now().plusDays(1).withHour(23).withMinute(0).withSecond(0).withNano(0)); // Depois das 22h
        
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility));
        
        // When/Then
        assertThatThrownBy(() -> rentalService.createRental(invalidRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("outside facility operating hours");
    }

    @Test
    public void whenCancelRental_alreadyPassed_shouldThrowException() {
        // Given - Rental já passou
        mockRental.setStartTime(LocalDateTime.now().minusDays(2));
        mockRental.setEndTime(LocalDateTime.now().minusDays(2).plusHours(2));
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));
        
        // When/Then
        assertThatThrownBy(() -> rentalService.cancelRental(1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot cancel rental that has already passed");
    }

    @Test
    public void whenCreateRental_partialOverlap_shouldThrowException() {
        // Given - Nova rental sobrepõe-se parcialmente com existente
        Rental existingRental = new Rental();
        existingRental.setId(2L);
        existingRental.setStatus("CONFIRMED");
        existingRental.setStartTime(startTime.minusHours(1)); // 18:00-20:00
        existingRental.setEndTime(startTime.plusHours(1));
        
        validRequest.setStartTime(startTime); // 19:00-21:00 (sobrepõe)
        validRequest.setEndTime(endTime);
        
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility));
        when(rentalRepository.findByFacilityIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                anyLong(), any(), any())).thenReturn(List.of(existingRental));
        
        // When/Then
        assertThatThrownBy(() -> rentalService.createRental(validRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already booked");
    }

    @Test
    public void whenCreateRental_exactTimeMatch_shouldThrowException() {
        // Given - Mesma hora exata de outra rental
        Rental existingRental = new Rental();
        existingRental.setId(2L);
        existingRental.setStatus("CONFIRMED");
        existingRental.setStartTime(startTime);
        existingRental.setEndTime(endTime);
        
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(mockFacility));
        when(rentalRepository.findByFacilityIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                anyLong(), any(), any())).thenReturn(List.of(existingRental));
        
        // When/Then
        assertThatThrownBy(() -> rentalService.createRental(validRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already booked");
    }
}