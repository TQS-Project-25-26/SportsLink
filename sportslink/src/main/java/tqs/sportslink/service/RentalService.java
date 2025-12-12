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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RentalService {

    private static final Logger logger = LoggerFactory.getLogger(RentalService.class);

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
        validateCreateRentalRequest(request);

        Facility facility = getFacilityOrThrow(request.getFacilityId());
        validateWithinOperatingHours(facility, request);

        ensureNoBookingConflict(request);

        User user = getUserOrThrow(request.getUserId());

        Rental rental = buildBaseRental(request, facility, user);

        List<Equipment> equipments = handleEquipmentSelectionAndStock(request);
        if (!equipments.isEmpty()) {
            rental.setEquipments(equipments);
        }

        applyPricingAndPaymentStatus(rental, request, facility, equipments);

        Rental saved = rentalRepository.save(rental);
        logger.info("Created rental id={} for user {} at facility {}", saved.getId(), user.getEmail(), facility.getId());
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
        logger.info("Cancelled rental id={}", rentalId);
        return mapToResponseDTO(updated);
    }

    public RentalResponseDTO updateRental(Long rentalId, RentalRequestDTO request) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_RENTAL_NOT_FOUND));

        validateUpdateRequestTimes(request);

        Facility facility = getFacilityOrThrow(request.getFacilityId());
        validateWithinOperatingHours(facility, request);

        ensureNoUpdateConflict(rentalId, request);

        handleEquipmentUpdateAndStock(rental, request);

        rental.setStartTime(request.getStartTime());
        rental.setEndTime(request.getEndTime());

        Rental updated = rentalRepository.save(rental);
        logger.info("Updated rental id={} new start={} end={}", rentalId, request.getStartTime(), request.getEndTime());
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

    private void validateCreateRentalRequest(RentalRequestDTO request) {
        LocalDateTime now = LocalDateTime.now();

        if (request.getStartTime().isBefore(now)) {
            throw new IllegalArgumentException("Cannot create rental in the past");
        }

        if (request.getEndTime().isBefore(request.getStartTime()) ||
                request.getEndTime().equals(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        if (request.getStartTime().plusHours(1).isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("Rental duration must be at least 1 hour");
        }

        if (request.getStartTime().plusHours(4).isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Rental duration cannot exceed 4 hours");
        }

        if (request.getStartTime().isBefore(now.plusHours(1))) {
            throw new IllegalArgumentException("Rental must be booked at least 1 hour in advance");
        }

        if (request.getStartTime().isAfter(now.plusDays(30))) {
            throw new IllegalArgumentException("Rental cannot be booked more than 30 days in advance");
        }
    }

    private Facility getFacilityOrThrow(Long facilityId) {
        return facilityRepository.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private void validateWithinOperatingHours(Facility facility, RentalRequestDTO request) {
        if (facility.getOpeningTime() != null && facility.getClosingTime() != null &&
                (request.getStartTime().toLocalTime().isBefore(facility.getOpeningTime()) ||
                        request.getEndTime().toLocalTime().isAfter(facility.getClosingTime()))) {
            throw new IllegalArgumentException("Rental time is outside facility operating hours");
        }
    }

    private void ensureNoBookingConflict(RentalRequestDTO request) {
        List<Rental> conflictingRentals = rentalRepository
                .findByFacilityIdAndStartTimeLessThanAndEndTimeGreaterThan(
                        request.getFacilityId(), request.getEndTime(), request.getStartTime());

        boolean hasConflict = conflictingRentals.stream()
                .anyMatch(r -> !STATUS_CANCELLED.equals(r.getStatus()));

        if (hasConflict) {
            throw new IllegalArgumentException("Facility already booked for this time slot");
        }
    }

    private Rental buildBaseRental(RentalRequestDTO request, Facility facility, User user) {
        Rental rental = new Rental();
        rental.setFacility(facility);
        rental.setStartTime(request.getStartTime());
        rental.setEndTime(request.getEndTime());
        rental.setStatus("CONFIRMED");
        rental.setUser(user);
        return rental;
    }

    private List<Equipment> handleEquipmentSelectionAndStock(RentalRequestDTO request) {
        if (request.getEquipmentIds() == null || request.getEquipmentIds().isEmpty()) {
            return new ArrayList<>();
        }

        List<Equipment> equipments = equipmentRepository.findAllById(request.getEquipmentIds());

        for (Equipment equip : equipments) {
            if (equip.getQuantity() <= 0) {
                throw new IllegalArgumentException("Equipment " + equip.getName() + " is out of stock");
            }
            equip.setQuantity(equip.getQuantity() - 1);
        }

        equipmentRepository.saveAll(equipments);
        return equipments;
    }

    private void applyPricingAndPaymentStatus(Rental rental,
                                            RentalRequestDTO request,
                                            Facility facility,
                                            List<Equipment> equipments) {

        double durationHours = java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes() / 60.0;

        double facilityPrice = facility.getPricePerHour() != null ? facility.getPricePerHour() * durationHours : 0.0;

        double equipmentPrice = equipments.stream()
                .mapToDouble(eq -> eq.getPricePerHour() != null ? eq.getPricePerHour() * durationHours : 0.0)
                .sum();

        rental.setTotalPrice(facilityPrice + equipmentPrice);
        rental.setPaymentStatus("UNPAID");
    }

    private void validateUpdateRequestTimes(RentalRequestDTO request) {
        LocalDateTime now = LocalDateTime.now();

        if (request.getStartTime().isBefore(now)) {
            throw new IllegalArgumentException("Cannot update rental to past time");
        }

        if (request.getStartTime().isBefore(now.plusHours(24))) {
            throw new IllegalArgumentException("Updates must be made for a time at least 24 hours in the future");
        }

        if (request.getEndTime().isBefore(request.getStartTime()) ||
                request.getEndTime().equals(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    private void ensureNoUpdateConflict(Long rentalId, RentalRequestDTO request) {
        List<Rental> conflictingRentals = rentalRepository
                .findByFacilityIdAndStartTimeLessThanAndEndTimeGreaterThan(
                        request.getFacilityId(), request.getEndTime(), request.getStartTime());

        boolean hasConflict = conflictingRentals.stream()
                .filter(r -> !r.getId().equals(rentalId)) // Excluir o próprio rental
                .anyMatch(r -> !STATUS_CANCELLED.equals(r.getStatus()));

        if (hasConflict) {
            throw new IllegalArgumentException("New time slot conflicts with existing booking");
        }
    }

    private void handleEquipmentUpdateAndStock(Rental rental, RentalRequestDTO request) {
        if (request.getEquipmentIds() == null) {
            return; // EXACT same behavior: null means "do nothing with equipments"
        }

        // 1. Devolver equipamentos antigos ao stock
        returnOldEquipmentsToStock(rental);

        // 2. Atribuir novos equipamentos e decrementar stock
        List<Equipment> newEquipments = reserveRequestedEquipments(request.getEquipmentIds());
        rental.setEquipments(newEquipments);
    }

    private void returnOldEquipmentsToStock(Rental rental) {
        if (rental.getEquipments() == null) {
            return;
        }

        for (Equipment eq : rental.getEquipments()) {
            eq.setQuantity(eq.getQuantity() + 1);
        }
        equipmentRepository.saveAll(rental.getEquipments());
    }

    private List<Equipment> reserveRequestedEquipments(List<Long> equipmentIds) {
        List<Equipment> newEquipments = new ArrayList<>();

        if (equipmentIds.isEmpty()) {
            return newEquipments; // EXACT same behavior: empty list => setEquipments(empty)
        }

        List<Equipment> requestedEquipments = equipmentRepository.findAllById(equipmentIds);

        for (Equipment eq : requestedEquipments) {
            if (eq.getQuantity() <= 0) {
                throw new IllegalArgumentException("Equipment " + eq.getName() + " is out of stock");
            }
            eq.setQuantity(eq.getQuantity() - 1);
            newEquipments.add(eq);
        }

        equipmentRepository.saveAll(newEquipments);
        return newEquipments;
    }


}
