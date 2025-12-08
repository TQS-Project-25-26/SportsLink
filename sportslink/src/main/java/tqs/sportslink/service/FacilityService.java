package tqs.sportslink.service;

import org.springframework.stereotype.Service;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.RentalRepository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Rental;
import tqs.sportslink.data.model.Sport;
import tqs.sportslink.dto.FacilityResponseDTO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final RentalRepository rentalRepository;

    public FacilityService(FacilityRepository facilityRepository, RentalRepository rentalRepository) {
        this.facilityRepository = facilityRepository;
        this.rentalRepository = rentalRepository;
    }

    public List<FacilityResponseDTO> searchFacilities(String location, String sport, String time) {
        List<Facility> facilities;
        
        // Handle optional parameters
        if (location != null && !location.isEmpty() && sport != null && !sport.isEmpty()) {
            Sport sportEnum = Sport.valueOf(sport.toUpperCase());
            facilities = facilityRepository.findByCityAndSportType(location, sportEnum);
        } else if (location != null && !location.isEmpty()) {
            facilities = facilityRepository.findByCity(location);
        } else if (sport != null && !sport.isEmpty()) {
            Sport sportEnum = Sport.valueOf(sport.toUpperCase());
            facilities = facilityRepository.findBySportType(sportEnum);
        } else {
            // No filters - return all
            facilities = facilityRepository.findAll();
        }
        
        // Filtrar ACTIVE no service - LÓGICA DE NEGÓCIO
        List<Facility> activeFacilities = facilities.stream()
            .filter(f -> "ACTIVE".equals(f.getStatus()))
            .toList();
        
        // If time parameter is provided, filter by availability
        if (time != null && !time.isEmpty()) {
            LocalDateTime requestedTime = parseTimeParameter(time);
            LocalDateTime searchWindowEnd = requestedTime.plusHours(3);
            
            return activeFacilities.stream()
                .filter(f -> hasAvailabilityInWindow(f, requestedTime, searchWindowEnd))
                .map(this::toDTO)
                .toList();
        }
        
        return activeFacilities.stream()
            .map(this::toDTO)
            .toList();
    }
    
    private LocalDateTime parseTimeParameter(String time) {
        try {
            // Try parsing as ISO datetime first (e.g., "2025-12-03T14:00:00")
            return LocalDateTime.parse(time);
        } catch (Exception e1) {
            try {
                // Try parsing with custom format (e.g., "2025-12-03 14:00")
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                return LocalDateTime.parse(time, formatter);
            } catch (Exception e2) {
                try {
                    // Try parsing just time (e.g., "19:00") - use current date
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    return LocalDateTime.now().with(java.time.LocalTime.parse(time, timeFormatter));
                } catch (Exception e3) {
                    throw new IllegalArgumentException("Invalid time format. Use ISO format (e.g., '2025-12-03T14:00:00'), 'yyyy-MM-dd HH:mm', or just time 'HH:mm'");
                }
            }
        }
    }
    
    private boolean hasAvailabilityInWindow(Facility facility, LocalDateTime windowStart, LocalDateTime windowEnd) {
        // Check if facility has operating hours and if the window fits within them
        if (facility.getOpeningTime() != null && facility.getClosingTime() != null) {
            // Check if any part of the 3-hour window falls within operating hours
            boolean windowStartInHours = !windowStart.toLocalTime().isBefore(facility.getOpeningTime()) 
                && !windowStart.toLocalTime().isAfter(facility.getClosingTime());
            boolean windowEndInHours = !windowEnd.toLocalTime().isBefore(facility.getOpeningTime()) 
                && !windowEnd.toLocalTime().isAfter(facility.getClosingTime());
                
            if (!windowStartInHours && !windowEndInHours) {
                return false; // Entire window is outside operating hours
            }
        }
        
        // Get all confirmed rentals for this facility
        List<Rental> rentals = rentalRepository.findByFacilityId(facility.getId()).stream()
            .filter(r -> !"CANCELLED".equals(r.getStatus()))
            .toList();
        
        // Check if there's at least one 1-hour slot available in the 3-hour window
        LocalDateTime currentSlot = windowStart;
        while (currentSlot.plusHours(1).isBefore(windowEnd) || currentSlot.plusHours(1).equals(windowEnd)) {
            LocalDateTime slotStart = currentSlot;
            LocalDateTime slotEnd = currentSlot.plusHours(1);
            
            // Check if this slot conflicts with any rental
            boolean hasConflict = rentals.stream()
                .anyMatch(rental -> 
                    // Rental overlaps with this slot
                    rental.getStartTime().isBefore(slotEnd) && rental.getEndTime().isAfter(slotStart)
                );
            
            if (!hasConflict) {
                return true; // Found an available slot
            }
            
            currentSlot = currentSlot.plusMinutes(30); // Check in 30-minute increments
        }
        
        return false; // No available slots found
    }
    
    private FacilityResponseDTO toDTO(Facility facility) {
        FacilityResponseDTO dto = new FacilityResponseDTO(
            facility.getId(),
            facility.getName(),
            facility.getImageUrl(),
            facility.getSports(),
            facility.getCity(),
            facility.getAddress(),
            facility.getDescription(),
            facility.getPricePerHour(),
            facility.getRating()
        );
        if (facility.getOpeningTime() != null) {
            dto.setOpeningTime(facility.getOpeningTime().toString());
        }
        if (facility.getClosingTime() != null) {
            dto.setClosingTime(facility.getClosingTime().toString());
        }
        return dto;
    }
}
