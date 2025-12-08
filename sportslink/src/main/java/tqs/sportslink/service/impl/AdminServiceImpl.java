package tqs.sportslink.service.impl;

import org.springframework.stereotype.Service;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.RentalRepository;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Rental;
import tqs.sportslink.data.model.User;
import tqs.sportslink.service.AdminService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final RentalRepository rentalRepository;

    public AdminServiceImpl(UserRepository userRepository, FacilityRepository facilityRepository,
            RentalRepository rentalRepository) {
        this.userRepository = userRepository;
        this.facilityRepository = facilityRepository;
        this.rentalRepository = rentalRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUserStatus(Long id, boolean active) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(active);
        return userRepository.save(user);
    }

    @Override
    public List<Facility> getAllFacilities() {
        return facilityRepository.findAll();
    }

    @Override
    public void deleteFacility(Long id) {
        facilityRepository.deleteById(id);
    }

    @Override
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalFacilities", facilityRepository.count());
        stats.put("totalRentals", rentalRepository.count());
        return stats;
    }

    @Override
    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    @Override
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

    @Override
    public Map<String, Long> getRentalsByStatus() {
        List<Rental> rentals = rentalRepository.findAll();
        Map<String, Long> stats = new HashMap<>();

        for (Rental rental : rentals) {
            String status = rental.getStatus();
            stats.put(status, stats.getOrDefault(status, 0L) + 1);
        }
        return stats;
    }

    @Override
    public User getUserDetails(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    public Facility getFacilityDetails(Long id) {
        return facilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));
    }

    @Override
    public Rental cancelRental(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found"));
        rental.setStatus("CANCELLED");
        return rentalRepository.save(rental);
    }
}
