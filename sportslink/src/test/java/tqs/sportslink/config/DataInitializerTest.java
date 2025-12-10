package tqs.sportslink.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import tqs.sportslink.data.EquipmentRepository;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Role;
import tqs.sportslink.data.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private tqs.sportslink.data.RentalRepository rentalRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void testInitDatabase_CreateAdmin_WhenNotExists() throws Exception {
        // Arrange
        when(userRepository.existsByEmail("admin@admin.com")).thenReturn(false);
        when(facilityRepository.count()).thenReturn(1L); // Prevent sample data creation to keep test simple

        // Act
        CommandLineRunner runner = dataInitializer.initDatabase(facilityRepository, equipmentRepository,
                userRepository, rentalRepository);
        runner.run();

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture()); // Only admin should be saved because count > 0
                                                                     // returns early

        User savedUser = userCaptor.getValue();
        assertEquals("admin@admin.com", savedUser.getEmail());
        assertTrue(savedUser.getRoles().contains(Role.ADMIN));
        // Password check is tricky due to BCrypt random salt, but we verify the
        // interactions
    }

    @Test
    void testInitDatabase_SkipAdmin_WhenExists() throws Exception {
        // Arrange
        when(userRepository.existsByEmail("admin@admin.com")).thenReturn(true);
        when(facilityRepository.count()).thenReturn(1L); // Prevent sample data creation

        // Act
        CommandLineRunner runner = dataInitializer.initDatabase(facilityRepository, equipmentRepository,
                userRepository, rentalRepository);
        runner.run();

        // Assert
        verify(userRepository, never()).save(any(User.class));
    }
}
