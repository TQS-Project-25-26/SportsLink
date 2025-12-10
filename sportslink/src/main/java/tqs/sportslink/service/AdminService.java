package tqs.sportslink.service;

import org.springframework.stereotype.Service;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.RentalRepository;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Rental;
import tqs.sportslink.data.model.User;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final RentalRepository rentalRepository;

    public AdminService(UserRepository userRepository, FacilityRepository facilityRepository,
            RentalRepository rentalRepository) {
        this.userRepository = userRepository;
        this.facilityRepository = facilityRepository;
        this.rentalRepository = rentalRepository;
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public User updateUserStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Utilizador autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String currentUsername = auth.getName(); // normalmente o email / username

            // Impedir que o admin desative a própria conta
            if (!active && currentUsername != null && currentUsername.equals(user.getEmail())) {
                throw new IllegalStateException("You cannot deactivate your own account.");
            }
        }

        user.setActive(active);

        logger.info("Admin updated user {} (id={}) status to active={}", user.getEmail(), id, active);
        return userRepository.save(user);
    }



    public List<Facility> getAllFacilities() {
        return facilityRepository.findAll();
    }


    public void deleteFacility(Long id) {
        logger.info("Admin deleting facility with id={}", id);
        facilityRepository.deleteById(id);
    }


    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalFacilities", facilityRepository.count());
        stats.put("totalRentals", rentalRepository.count());
        return stats;
    }


    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }


    public Map<String, Long> getRentalsBySport() {
        List<Rental> rentals = rentalRepository.findAll();
        Map<String, Long> stats = new HashMap<>();

        for (Rental rental : rentals) {
            String sportName = "Unknown";
            Facility facility = rental.getFacility();

            // If facility has sports, use the first one as primary, or count for all?
            // For simplicity and chart clarity, let's pick the first one or "Mixed"
            if (facility != null && facility.getSports() != null && !facility.getSports().isEmpty()) {
                sportName = facility.getSports().get(0).name();
            }

            stats.put(sportName, stats.getOrDefault(sportName, 0L) + 1);
        }
        return stats;
    }


    public Map<String, Long> getRentalsByStatus() {
        List<Rental> rentals = rentalRepository.findAll();
        Map<String, Long> stats = new HashMap<>();

        for (Rental rental : rentals) {
            String status = rental.getStatus();
            stats.put(status, stats.getOrDefault(status, 0L) + 1);
        }
        return stats;
    }


    public User getUserDetails(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }


    public Facility getFacilityDetails(Long id) {
        return facilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));
    }


    public Rental cancelRental(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found"));
        rental.setStatus("CANCELLED");
        logger.info("Admin cancelled rental id={}", id);
        return rentalRepository.save(rental);
    }
}