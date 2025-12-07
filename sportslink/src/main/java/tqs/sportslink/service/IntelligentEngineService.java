package tqs.sportslink.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.RentalRepository;
import tqs.sportslink.data.EquipmentRepository;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.*;
import tqs.sportslink.dto.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IntelligentEngineService {

    private static final Logger logger = LoggerFactory.getLogger(IntelligentEngineService.class);

    private final FacilityRepository facilityRepository;
    private final RentalRepository rentalRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    public IntelligentEngineService(
            FacilityRepository facilityRepository,
            RentalRepository rentalRepository,
            EquipmentRepository equipmentRepository,
            UserRepository userRepository) {
        this.facilityRepository = facilityRepository;
        this.rentalRepository = rentalRepository;
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get personalized facility suggestions for a user based on:
     * - Location (City match or proximity)
     * - Sports preferences (previous sports)
     * - Rating
     */
    public List<FacilitySuggestionDTO> suggestFacilitiesForUser(Long userId) {
        return suggestFacilitiesForUser(userId, null, null);
    }

    /**
     * Get personalized facility suggestions for a user with optional current
     * location
     */
    public List<FacilitySuggestionDTO> suggestFacilitiesForUser(Long userId, Double userLat, Double userLon) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Use provided coordinates or fall back to user's saved location
        Double effectiveLat = userLat != null ? userLat : user.getLatitude();
        Double effectiveLon = userLon != null ? userLon : user.getLongitude();

        List<Rental> userRentals = rentalRepository.findByUserId(userId);

        if (userRentals.isEmpty()) {
            return suggestTopRatedFacilities();
        }

        // Analyze user's preferences
        Map<String, Long> cityCounts = userRentals.stream()
                .collect(Collectors.groupingBy(r -> r.getFacility().getCity(), Collectors.counting()));

        String preferredCity = cityCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        Set<Sport> preferredSports = userRentals.stream()
                .flatMap(r -> r.getFacility().getSports().stream())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Sport, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        // Get candidate facilities (filtering out visited ones)
        Set<Long> visitedFacilityIds = userRentals.stream()
                .map(r -> r.getFacility().getId())
                .collect(Collectors.toSet());

        List<Facility> candidates = facilityRepository.findAll().stream()
                .filter(f -> "ACTIVE".equals(f.getStatus()))
                .filter(f -> !visitedFacilityIds.contains(f.getId()))
                .collect(Collectors.toList());

        // Score facilities
        List<FacilitySuggestionDTO> suggestions = candidates.stream()
                .map(facility -> createFacilitySuggestion(facility, preferredCity, preferredSports, effectiveLat,
                        effectiveLon))
                .sorted(Comparator.comparingDouble(FacilitySuggestionDTO::getScore).reversed())
                .limit(5)
                .collect(Collectors.toList());

        return suggestions;
    }

    /**
     * Suggest equipment strictly based on the sport being played.
     * ONLY equipment explicitly linked to the sport will be suggested.
     */
    public List<EquipmentSuggestionDTO> suggestEquipmentForSport(Long facilityId, String sportName) {
        logger.info("Suggesting equipment for facility {} and sport {}", facilityId, sportName);

        Sport sport;
        try {
            sport = Sport.valueOf(sportName.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid sport: {}", sportName);
            return Collections.emptyList();
        }

        return equipmentRepository.findByFacilityId(facilityId).stream()
                .filter(e -> "AVAILABLE".equals(e.getStatus()))
                .filter(e -> e.getSports() != null && e.getSports().contains(sport))
                .map(e -> new EquipmentSuggestionDTO(
                        e.getId(),
                        e.getName(),
                        e.getType(),
                        e.getPricePerHour(),
                        e.getQuantity(),
                        "Essential for " + sportName,
                        95.0 // High score for strict matches
                ))
                .sorted(Comparator.comparingDouble(EquipmentSuggestionDTO::getScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Provide simplified suggestions for facility owners
     */
    public List<OwnerSuggestionDTO> suggestImprovementsForOwner(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

        if (!Role.OWNER.equals(owner.getRole())) {
            throw new IllegalArgumentException("User is not an owner");
        }

        List<OwnerSuggestionDTO> suggestions = new ArrayList<>();

        for (Facility facility : owner.getFacilities()) {
            List<Rental> rentals = rentalRepository.findByFacilityId(facility.getId());

            // 1. Demand Analysis
            long recentBookings = rentals.stream()
                    .filter(r -> r.getCreatedAt().isAfter(LocalDateTime.now().minusDays(30)))
                    .count();

            if (recentBookings > 20) {
                // High demand -> Suggest adding equipment if low stock
                long equipmentCount = equipmentRepository.findByFacilityId(facility.getId()).size();
                if (equipmentCount < 5) {
                    suggestions.add(new OwnerSuggestionDTO(
                            "ADD_EQUIPMENT",
                            facility.getId(),
                            facility.getName(),
                            "High Demand Detected",
                            "High booking volume detected. Add more equipment to maximize revenue.",
                            "HIGH",
                            facility.getPricePerHour() * 10));
                }
            } else if (recentBookings < 5) {
                suggestions.add(new OwnerSuggestionDTO(
                        "LOWER_PRICE",
                        facility.getId(),
                        facility.getName(),
                        "Low Utilization",
                        "Few bookings recently. Consider a promotion or price reduction.",
                        "MEDIUM",
                        null));
            }

            // 2. Maintenance Analysis
            boolean needsMaintenance = false;
            if (facility.getRating() != null && facility.getRating() < 4.0) {
                needsMaintenance = true;
            } else {
                long daysSinceUpdate = ChronoUnit.DAYS.between(facility.getUpdatedAt(), LocalDateTime.now());
                if (daysSinceUpdate > 90)
                    needsMaintenance = true;
            }

            if (needsMaintenance) {
                suggestions.add(new OwnerSuggestionDTO(
                        "MAINTENANCE",
                        facility.getId(),
                        facility.getName(),
                        "Maintenance / Quality Check",
                        "Facility rating is low or information hasn't been updated in 3 months.",
                        "HIGH",
                        null));
            }
        }

        return suggestions;
    }

    private List<FacilitySuggestionDTO> suggestTopRatedFacilities() {
        return facilityRepository.findAll().stream()
                .filter(f -> "ACTIVE".equals(f.getStatus()))
                .filter(f -> f.getRating() != null && f.getRating() > 4.0)
                .sorted(Comparator.comparingDouble(Facility::getRating).reversed())
                .limit(5)
                .map(f -> new FacilitySuggestionDTO(
                        f.getId(),
                        f.getName(),
                        f.getAddress(),
                        f.getCity(),
                        f.getPricePerHour(),
                        f.getRating(),
                        "Top rated facility",
                        f.getRating() * 20,
                        null))
                .collect(Collectors.toList());
    }

    private FacilitySuggestionDTO createFacilitySuggestion(Facility facility, String preferredCity,
            Set<Sport> preferredSports, Double userLat, Double userLon) {
        double score = 0.0;
        List<String> reasons = new ArrayList<>();
        Double distance = null;

        // Location Score (40 pts)
        if (userLat != null && userLon != null && facility.getLatitude() != null && facility.getLongitude() != null) {
            distance = calculateDistance(userLat, userLon, facility.getLatitude(), facility.getLongitude());
            if (distance < 20.0) {
                score += 40.0 * (1 - (distance / 20.0));
                reasons.add(String.format("%.1f km away", distance));
            }
        } else if (preferredCity != null && preferredCity.equalsIgnoreCase(facility.getCity())) {
            score += 40.0;
            reasons.add("In " + preferredCity);
        }

        // Sport Match Score (30 pts)
        boolean sportMatch = facility.getSports().stream().anyMatch(preferredSports::contains);
        if (sportMatch) {
            score += 30.0;
            reasons.add("Matches your sports");
        }

        // Rating Score (30 pts)
        if (facility.getRating() != null) {
            score += (facility.getRating() / 5.0) * 30.0;
        }

        return new FacilitySuggestionDTO(
                facility.getId(),
                facility.getName(),
                facility.getAddress(),
                facility.getCity(),
                facility.getPricePerHour(),
                facility.getRating(),
                String.join(", ", reasons),
                score,
                distance);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS_KM = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
