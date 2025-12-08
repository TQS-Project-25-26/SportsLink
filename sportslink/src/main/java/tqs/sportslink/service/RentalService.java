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
import java.util.ArrayList;
import java.util.List;


@Service
public class RentalService {

    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String ERROR_RENTAL_NOT_FOUND = "Rental not found";

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
        
        // Validação: duração máxima (4 horas)
        if (request.getStartTime().plusHours(4).isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Rental duration cannot exceed 4 hours");
        }
        
        // Validação: mínimo de antecedência (1 hora)
        if (request.getStartTime().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new IllegalArgumentException("Rental must be booked at least 1 hour in advance");
        }
        
        // Validação: máximo de antecedência (30 dias)
        if (request.getStartTime().isAfter(LocalDateTime.now().plusDays(30))) {
            throw new IllegalArgumentException("Rental cannot be booked more than 30 days in advance");
        }
        
        // Buscar facility primeiro para validar horários de funcionamento
        Facility facility = facilityRepository.findById(request.getFacilityId())
            .orElseThrow(() -> new IllegalArgumentException("Facility not found"));
        
        // Validação: horários de funcionamento da facility
        if (facility.getOpeningTime() != null && facility.getClosingTime() != null &&
            (request.getStartTime().toLocalTime().isBefore(facility.getOpeningTime()) ||
             request.getEndTime().toLocalTime().isAfter(facility.getClosingTime()))) {
            throw new IllegalArgumentException("Rental time is outside facility operating hours");
        }
        
        // Validar conflitos - LÓGICA NO SERVICE
        List<Rental> conflictingRentals = rentalRepository
            .findByFacilityIdAndStartTimeLessThanAndEndTimeGreaterThan(
                request.getFacilityId(), request.getEndTime(), request.getStartTime()
            );
        
        // Filtrar rentals cancelados
        boolean hasConflict = conflictingRentals.stream()
            .anyMatch(r -> !STATUS_CANCELLED.equals(r.getStatus()));
        
        if (hasConflict) {
            throw new IllegalArgumentException("Facility already booked for this time slot");
        }

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
            
            // Verificar disponibilidade e decrementar stock
            for (Equipment equip : equipments) {
                if (equip.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Equipment " + equip.getName() + " is out of stock");
                }
                equip.setQuantity(equip.getQuantity() - 1);
            }
            // Guardar alterações de stock
            equipmentRepository.saveAll(equipments);
            
            rental.setEquipments(equipments);
        }

        Rental saved = rentalRepository.save(rental);
        return mapToResponseDTO(saved);
    }

    public RentalResponseDTO cancelRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_RENTAL_NOT_FOUND));
        
        // Validação: não cancelar rental já cancelado
        if (STATUS_CANCELLED.equals(rental.getStatus())) {
            throw new IllegalArgumentException("Rental is already cancelled");
        }
        
        // Validação: não cancelar rental que já passou
        if (rental.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot cancel rental that has already passed");
        }
        
        rental.setStatus(STATUS_CANCELLED);
        Rental updated = rentalRepository.save(rental);
        return mapToResponseDTO(updated);
    }

    public RentalResponseDTO updateRental(Long rentalId, RentalRequestDTO request) {
        Rental rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_RENTAL_NOT_FOUND));
        
        // Validação: não permitir atualizar para horário no passado
        // Validação: não permitir atualizar para horário no passado ou com menos de 24h
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot update rental to past time");
        }
        
        if (request.getStartTime().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new IllegalArgumentException("Updates must be made for a time at least 24 hours in the future");
        }
        
        // Validação: horário de término deve ser depois do início
        if (request.getEndTime().isBefore(request.getStartTime()) || 
            request.getEndTime().equals(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Buscar facility para validar horários de funcionamento
        Facility facility = facilityRepository.findById(request.getFacilityId())
            .orElseThrow(() -> new IllegalArgumentException("Facility not found"));

        // Validação: horários de funcionamento da facility
        if (facility.getOpeningTime() != null && facility.getClosingTime() != null &&
            (request.getStartTime().toLocalTime().isBefore(facility.getOpeningTime()) ||
             request.getEndTime().toLocalTime().isAfter(facility.getClosingTime()))) {
            throw new IllegalArgumentException("Rental time is outside facility operating hours");
        }
        
        // Validar novos horários - LÓGICA NO SERVICE (excluindo o próprio rental)
        List<Rental> conflictingRentals = rentalRepository
            .findByFacilityIdAndStartTimeLessThanAndEndTimeGreaterThan(
                request.getFacilityId(), request.getEndTime(), request.getStartTime()
            );
        
        boolean hasConflict = conflictingRentals.stream()
            .filter(r -> !r.getId().equals(rentalId)) // Excluir o próprio rental
            .anyMatch(r -> !STATUS_CANCELLED.equals(r.getStatus()));
        
        if (hasConflict) {
        throw new IllegalArgumentException("New time slot conflicts with existing booking");
    }

    // Atualizar Equipamentos (Gerir Stock)
    if (request.getEquipmentIds() != null) {
        // 1. Devolver equipamentos antigos ao stock
        if (rental.getEquipments() != null) {
            for (Equipment eq : rental.getEquipments()) {
                eq.setQuantity(eq.getQuantity() + 1);
            }
            equipmentRepository.saveAll(rental.getEquipments());
        }

        // 2. Atribuir novos equipamentos e decrementar stock
        List<Equipment> newEquipments = new ArrayList<>();
        if (!request.getEquipmentIds().isEmpty()) {
             List<Equipment> requestedEquipments = equipmentRepository.findAllById(request.getEquipmentIds());
             for (Equipment eq : requestedEquipments) {
                 if (eq.getQuantity() <= 0) {
                     // Reverter devolução anterior se falhar? 
                     // Idealmente sim, mas como é RuntimeException, o Transactional deve fazer rollback de tudo.
                     throw new IllegalArgumentException("Equipment " + eq.getName() + " is out of stock");
                 }
                 eq.setQuantity(eq.getQuantity() - 1);
                 newEquipments.add(eq);
             }
             equipmentRepository.saveAll(newEquipments);
        }
        rental.setEquipments(newEquipments);
    }

    rental.setStartTime(request.getStartTime());
    rental.setEndTime(request.getEndTime());
        Rental updated = rentalRepository.save(rental);
        return mapToResponseDTO(updated);
    }

    public RentalResponseDTO getRentalStatus(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_RENTAL_NOT_FOUND));
        return mapToResponseDTO(rental);
    }

    public List<RentalResponseDTO> getUserRentals(Long userId) {
        List<Rental> rentals = rentalRepository.findByUserId(userId);
        return rentals.stream()
            .map(this::mapToResponseDTO)
            .toList();
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
                .toList());
        }
        return dto;
    }
}
