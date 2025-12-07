package tqs.sportslink.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import tqs.sportslink.data.EquipmentRepository;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.User;
import tqs.sportslink.dto.FacilityRequestDTO;
import tqs.sportslink.dto.FacilityResponseDTO;
import tqs.sportslink.service.OwnerService;
import tqs.sportslink.service.StorageService;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UnitOwnerServiceTest {

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private OwnerService ownerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createFacility_WithImage_ShouldUploadAndSaveUrl() {
        // Arrange
        Long ownerId = 1L;
        User owner = new User();
        owner.setId(ownerId);
        owner.setEmail("owner@example.com");

        FacilityRequestDTO requestDTO = new FacilityRequestDTO();
        requestDTO.setName("Test Facility");
        requestDTO.setSports(Collections.emptyList());
        requestDTO.setCity("Aveiro");
        requestDTO.setAddress("Campus");
        requestDTO.setPricePerHour(10.0);
        requestDTO.setOpeningTime("08:00");
        requestDTO.setClosingTime("22:00");

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);

        String expectedUrl = "http://minio/bucket/image.jpg";
        when(storageService.uploadFile(mockFile)).thenReturn(expectedUrl);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        when(facilityRepository.save(any(Facility.class))).thenAnswer(invocation -> {
            Facility f = invocation.getArgument(0);
            f.setId(100L); // simulate save
            return f;
        });

        // Act
        FacilityResponseDTO response = ownerService.createFacility(ownerId, requestDTO, mockFile);

        // Assert
        assertNotNull(response);
        assertEquals(expectedUrl, response.getImageUrl());
        verify(storageService, times(1)).uploadFile(mockFile);
        verify(facilityRepository, times(1)).save(any(Facility.class));
    }

    @Test
    void createFacility_WithoutImage_ShouldNotUpload() {
        // Arrange
        Long ownerId = 1L;
        User owner = new User();
        owner.setId(ownerId);

        FacilityRequestDTO requestDTO = new FacilityRequestDTO();
        requestDTO.setName("No Image Facility");
        requestDTO.setSports(Collections.emptyList());
        requestDTO.setCity("Aveiro");
        requestDTO.setAddress("Campus");
        requestDTO.setPricePerHour(10.0);
        requestDTO.setOpeningTime("08:00");
        requestDTO.setClosingTime("22:00");

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(facilityRepository.save(any(Facility.class))).thenAnswer(invocation -> {
            Facility f = invocation.getArgument(0);
            f.setId(101L);
            return f;
        });

        // Act
        FacilityResponseDTO response = ownerService.createFacility(ownerId, requestDTO, null);

        // Assert
        assertNotNull(response);
        assertEquals(null, response.getImageUrl());
        verify(storageService, never()).uploadFile(any());
        verify(facilityRepository, times(1)).save(any(Facility.class));
    }
}
