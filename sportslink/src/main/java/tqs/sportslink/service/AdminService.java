package tqs.sportslink.service;

import tqs.sportslink.data.model.User;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Rental;
import java.util.List;
import java.util.Map;

public interface AdminService {

    List<User> getAllUsers();

    User updateUserStatus(Long id, boolean active);

    List<Facility> getAllFacilities();

    void deleteFacility(Long id);

    Map<String, Object> getSystemStats();

    List<Rental> getAllRentals();

    Map<String, Long> getRentalsBySport();

    Map<String, Long> getRentalsByStatus();

    User getUserDetails(Long id);

    Facility getFacilityDetails(Long id);

    Rental cancelRental(Long id);
}
