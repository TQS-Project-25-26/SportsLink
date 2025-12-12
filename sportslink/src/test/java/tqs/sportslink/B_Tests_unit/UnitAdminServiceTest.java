package tqs.sportslink.B_Tests_unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.RentalRepository;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Rental;
import tqs.sportslink.data.model.User;
import tqs.sportslink.service.AdminService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import app.getxray.xray.junit.customjunitxml.annotations.Requirement;

@ExtendWith(MockitoExtension.class)
class UnitAdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private RentalRepository rentalRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    @Requirement("SL-343")
    void testGetAllUsers() {
        User u1 = new User();
        u1.setEmail("u1@test.com");
        User u2 = new User();
        u2.setEmail("u2@test.com");
        when(userRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        List<User> result = adminService.getAllUsers();
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @Requirement("SL-343")
    void testUpdateUserStatus() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = adminService.updateUserStatus(1L, false);
        assertFalse(result.getActive());
        verify(userRepository).save(user);
    }

    @Test
    @Requirement("SL-343")
    void testGetAllFacilities() {
        Facility f1 = new Facility();
        when(facilityRepository.findAll()).thenReturn(Arrays.asList(f1));

        List<Facility> result = adminService.getAllFacilities();
        assertEquals(1, result.size());
        verify(facilityRepository, times(1)).findAll();
    }

    @Test
    @Requirement("SL-343")
    void testDeleteFacility() {
        doNothing().when(facilityRepository).deleteById(1L);
        adminService.deleteFacility(1L);
        verify(facilityRepository, times(1)).deleteById(1L);
    }

    @Test
    @Requirement("SL-343")
    void testGetSystemStats() {
        when(userRepository.count()).thenReturn(10L);
        when(facilityRepository.count()).thenReturn(5L);
        when(rentalRepository.count()).thenReturn(20L);

        Map<String, Object> stats = adminService.getSystemStats();
        assertEquals(10L, stats.get("totalUsers"));
        assertEquals(5L, stats.get("totalFacilities"));
        assertEquals(20L, stats.get("totalRentals"));
    }

    @Test
    @Requirement("SL-343")
    void testGetAllRentals() {
        Rental r1 = rental();
        when(rentalRepository.findAll()).thenReturn(Arrays.asList(r1));

        List<Rental> result = adminService.getAllRentals();
        assertEquals(1, result.size());
        verify(rentalRepository, times(1)).findAll();
    }

    @Test
    @Requirement("SL-343")
    void testGetRentalsBySport() {
        Facility f1 = new Facility();
        f1.setSports(List.of(tqs.sportslink.data.model.Sport.FOOTBALL));
        Rental r1 = new Rental();
        r1.setFacility(f1);

        Facility f2 = new Facility();
        f2.setSports(List.of(tqs.sportslink.data.model.Sport.TENNIS));
        Rental r2 = new Rental();
        r2.setFacility(f2);

        when(rentalRepository.findAll()).thenReturn(Arrays.asList(r1, r2));

        Map<String, Long> result = adminService.getRentalsBySport();
        assertEquals(1, result.get("FOOTBALL"));
        assertEquals(1, result.get("TENNIS"));
    }

    @Test
    @Requirement("SL-343")
    void testGetRentalsByStatus() {
        Rental r1 = new Rental();
        r1.setStatus("CONFIRMED");
        Rental r2 = new Rental();
        r2.setStatus("PENDING");

        when(rentalRepository.findAll()).thenReturn(Arrays.asList(r1, r2));

        Map<String, Long> result = adminService.getRentalsByStatus();
        assertEquals(1, result.get("CONFIRMED"));
        assertEquals(1, result.get("PENDING"));
    }

    @Test
    @Requirement("SL-343")
    void testGetUserDetails() {
        User u = new User();
        u.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        User result = adminService.getUserDetails(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    @Requirement("SL-343")
    void testGetFacilityDetails() {
        Facility f = new Facility();
        f.setId(1L);
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(f));

        Facility result = adminService.getFacilityDetails(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    @Requirement("SL-343")
    void testCancelRental() {
        Rental rental = new Rental();
        rental.setId(1L);
        rental.setStatus("CONFIRMED");

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(i -> i.getArguments()[0]);

        Rental result = adminService.cancelRental(1L);
        assertEquals("CANCELLED", result.getStatus());
        verify(rentalRepository).save(rental);
    }

    @Test
    @Requirement("SL-343")
    void whenAdminTriesToDeactivateOwnAccount_thenThrowIllegalState() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("admin@admin.com");
        user.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Simulate authenticated admin with same username/email
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin@admin.com", "pw", java.util.List.of());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            // Act + Assert
            org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                    () -> adminService.updateUserStatus(1L, false));

            // Ensure we never persist the change
            verify(userRepository, never()).save(any(User.class));
        } finally {
            // Cleanup to avoid leaking auth into other tests
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }


    private Rental rental() {
        return new Rental();
    }
}