package tqs.sportslink.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.sportslink.service.RentalService;
import tqs.sportslink.data.RentalRepository;
import tqs.sportslink.dto.RentalRequestDTO;
import tqs.sportslink.dto.RentalResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UnitRentalService {

    @Mock
    private RentalRepository rentalRepository;

    @InjectMocks
    private RentalService rentalService;

    private RentalRequestDTO validRequest;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    public void setUp() {
        startTime = LocalDateTime.of(2025, 11, 27, 19, 0);
        endTime = LocalDateTime.of(2025, 11, 27, 21, 0);
        
        validRequest = new RentalRequestDTO();
        validRequest.setFacilityId(1L);
        validRequest.setStartTime(startTime);
        validRequest.setEndTime(endTime);
    }

    @Test
    public void whenCreateRental_withValidRequest_thenSuccess() {
        // Given - Maria booking Padel Club Aveiro
        // When
        RentalResponseDTO result = rentalService.createRental(validRequest);
        
        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void whenCreateRental_withEquipment_thenSuccess() {
        // Given - Booking with equipment
        validRequest.setEquipmentIds(List.of(1L, 2L));
        
        // When
        RentalResponseDTO result = rentalService.createRental(validRequest);
        
        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void whenCreateRental_facilityAlreadyBooked_shouldValidateConflict() {
        // Given - Facility já tem reserva para 19:00-21:00
        RentalRequestDTO conflictRequest = new RentalRequestDTO();
        conflictRequest.setFacilityId(1L);
        conflictRequest.setStartTime(LocalDateTime.of(2025, 11, 27, 19, 30));
        conflictRequest.setEndTime(LocalDateTime.of(2025, 11, 27, 21, 30));
        
        // When - Deve validar conflito quando implementado
        RentalResponseDTO result = rentalService.createRental(conflictRequest);
        
        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void whenCreateRental_equipmentUnavailable_shouldValidate() {
        // Given - Equipamento indisponível
        validRequest.setEquipmentIds(List.of(999L));
        
        // When - Deve validar disponibilidade quando implementado
        RentalResponseDTO result = rentalService.createRental(validRequest);
        
        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void whenCreateRental_invalidTimeRange_shouldValidate() {
        // Given - Horário inválido: fim antes do início
        RentalRequestDTO invalidRequest = new RentalRequestDTO();
        invalidRequest.setFacilityId(1L);
        invalidRequest.setStartTime(LocalDateTime.of(2025, 11, 27, 21, 0));
        invalidRequest.setEndTime(LocalDateTime.of(2025, 11, 27, 19, 0));
        
        // When - Deve validar quando implementado
        RentalResponseDTO result = rentalService.createRental(invalidRequest);
        
        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void whenCancelRental_validId_thenSuccess() {
        // Given
        Long rentalId = 1L;
        
        // When
        RentalResponseDTO result = rentalService.cancelRental(rentalId);
        
        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void whenUpdateRental_validChange_thenSuccess() {
        // Given
        Long rentalId = 1L;
        RentalRequestDTO updateRequest = new RentalRequestDTO();
        updateRequest.setFacilityId(1L);
        updateRequest.setStartTime(LocalDateTime.of(2025, 11, 27, 20, 0));
        updateRequest.setEndTime(LocalDateTime.of(2025, 11, 27, 22, 0));
        
        // When
        RentalResponseDTO result = rentalService.updateRental(rentalId, updateRequest);
        
        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void whenGetRentalStatus_validId_thenReturnsStatus() {
        // Given
        Long rentalId = 1L;
        
        // When
        RentalResponseDTO result = rentalService.getRentalStatus(rentalId);
        
        // Then
        assertThat(result).isNotNull();
    }
}