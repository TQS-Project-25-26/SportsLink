package tqs.sportslink.C_Tests_controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import tqs.sportslink.boundary.OwnerController;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.User;
import tqs.sportslink.dto.FacilityRequestDTO;
import tqs.sportslink.dto.FacilityResponseDTO;
import tqs.sportslink.service.OwnerService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UnitOwnerControllerTest {

    @Mock
    private OwnerService ownerService;

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OwnerController ownerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createFacility_WithImage_ShouldInteractWithService() {
        // Arrange
        Long ownerId = 1L;
        String email = "owner@example.com";
        
        User mockUser = new User();
        mockUser.setId(ownerId);
        mockUser.setEmail(email);

        FacilityRequestDTO requestDTO = new FacilityRequestDTO();
        requestDTO.setName("Test Facility");
        
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "image data".getBytes()
        );

        FacilityResponseDTO expectedResponse = new FacilityResponseDTO();
        expectedResponse.setId(100L);
        expectedResponse.setName("Test Facility");
        expectedResponse.setImageUrl("http://minio/bucket/test.jpg");

        // Mock authentication and owner validation
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        when(ownerService.createFacility(eq(ownerId), any(FacilityRequestDTO.class), any(MultipartFile.class)))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<FacilityResponseDTO> response = ownerController.createFacility(ownerId, requestDTO, imageFile);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals("http://minio/bucket/test.jpg", response.getBody().getImageUrl());
        verify(ownerService, times(1)).createFacility(eq(ownerId), eq(requestDTO), eq(imageFile));
    }
}
