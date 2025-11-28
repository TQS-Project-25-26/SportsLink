package tqs.sportslink.service;

import org.springframework.stereotype.Service;
import tqs.sportslink.dto.RentalRequestDTO;
import tqs.sportslink.dto.RentalResponseDTO;
import tqs.sportslink.data.RentalRepository;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.EquipmentRepository;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Rental;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Equipment;
import tqs.sportslink.data.model.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;
    private final FacilityRepository facilityRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    public RentalService(RentalRepository rentalRepository, FacilityRepository facilityRepository, 
                        EquipmentRepository equipmentRepository, UserRepository userRepository) {
        this.rentalRepository = rentalRepository;
        this.facilityRepository = facilityRepository;
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
    }

    public RentalResponseDTO createRental(RentalRequestDTO request) {
        // Validação: não permitir reservas no passado
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot create rental in the past");
        }
        
        // Validação: horário de término deve ser depois do início
        if (request.getEndTime().isBefore(request.getStartTime()) || 
            request.getEndTime().equals(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        // Validação: duração mínima (1 hora)
        if (request.getStartTime().plusHours(1).isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("Rental duration must be at least 1 hour");
        }
        
        // Validar conflitos - LÓGICA NO SERVICE
        List<Rental> conflictingRentals = rentalRepository
            .findByFacilityIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                request.getFacilityId(), request.getEndTime(), request.getStartTime()
            );
        
        // Filtrar rentals cancelados
        boolean hasConflict = conflictingRentals.stream()
            .anyMatch(r -> !"CANCELLED".equals(r.getStatus()));
        
        if (hasConflict) {
            throw new IllegalArgumentException("Facility already booked for this time slot");
        }

        // Buscar facility
        Facility facility = facilityRepository.findById(request.getFacilityId())
            .orElseThrow(() -> new IllegalArgumentException("Facility not found"));

        // Criar rental
        Rental rental = new Rental();
        rental.setFacility(facility);
        rental.setStartTime(request.getStartTime());
        rental.setEndTime(request.getEndTime());
        rental.setStatus("CONFIRMED");
        
        // Buscar user do request
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        rental.setUser(user);

        // Adicionar equipamentos se houver
        if (request.getEquipmentIds() != null && !request.getEquipmentIds().isEmpty()) {
            List<Equipment> equipments = equipmentRepository.findAllById(request.getEquipmentIds());
            rental.setEquipments(equipments);
        }

        Rental saved = rentalRepository.save(rental);
        return mapToResponseDTO(saved);
    }

    public RentalResponseDTO cancelRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new IllegalArgumentException("Rental not found"));
        
        rental.setStatus("CANCELLED");
        Rental updated = rentalRepository.save(rental);
        return mapToResponseDTO(updated);
    }

    public RentalResponseDTO updateRental(Long rentalId, RentalRequestDTO request) {
        Rental rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new IllegalArgumentException("Rental not found"));
        
        // Validação: não permitir atualizar para horário no passado
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot update rental to past time");
        }
        
        // Validação: horário de término deve ser depois do início
        if (request.getEndTime().isBefore(request.getStartTime()) || 
            request.getEndTime().equals(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        // Validar novos horários - LÓGICA NO SERVICE (excluindo o próprio rental)
        List<Rental> conflictingRentals = rentalRepository
            .findByFacilityIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                request.getFacilityId(), request.getEndTime(), request.getStartTime()
            );
        
        boolean hasConflict = conflictingRentals.stream()
            .filter(r -> !r.getId().equals(rentalId)) // Excluir o próprio rental
            .anyMatch(r -> !"CANCELLED".equals(r.getStatus()));
        
        if (hasConflict) {
            throw new IllegalArgumentException("New time slot conflicts with existing booking");
        }

        rental.setStartTime(request.getStartTime());
        rental.setEndTime(request.getEndTime());
        Rental updated = rentalRepository.save(rental);
        return mapToResponseDTO(updated);
    }

    public RentalResponseDTO getRentalStatus(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new IllegalArgumentException("Rental not found"));
        return mapToResponseDTO(rental);
    }

    private RentalResponseDTO mapToResponseDTO(Rental rental) {
        RentalResponseDTO dto = new RentalResponseDTO();
        dto.setId(rental.getId());
        dto.setUserId(rental.getUser().getId());
        dto.setFacilityId(rental.getFacility().getId());
        dto.setStartTime(rental.getStartTime());
        dto.setEndTime(rental.getEndTime());
        dto.setStatus(rental.getStatus());
        if (rental.getEquipments() != null) {
            dto.setEquipments(rental.getEquipments().stream()
                .map(Equipment::getName)
                .collect(Collectors.toList()));
        }
        return dto;
    }
}
