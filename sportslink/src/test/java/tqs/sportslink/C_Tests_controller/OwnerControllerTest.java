package tqs.sportslink.C_Tests_controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tqs.sportslink.boundary.OwnerController;
import tqs.sportslink.config.TestSecurityConfig;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.User;
import tqs.sportslink.dto.FacilityRequestDTO;
import tqs.sportslink.dto.FacilityResponseDTO;
import tqs.sportslink.service.OwnerService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OwnerController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class OwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OwnerService ownerService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Mock user for getAuthenticatedOwnerId()
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("owner@example.com");
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(mockUser));
    }

    @Test
    @WithMockUser(username = "owner@example.com", roles = {"OWNER"})
    void createFacility_WithImage_ShouldReturn200() throws Exception {
        // Arrange
        Long ownerId = 1L;
        FacilityRequestDTO requestDTO = new FacilityRequestDTO();
        requestDTO.setName("Test Facility");
        
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "image data".getBytes()
        );
        
        MockMultipartFile jsonFile = new MockMultipartFile(
                "facility", "", "application/json", objectMapper.writeValueAsBytes(requestDTO)
        );

        FacilityResponseDTO expectedResponse = new FacilityResponseDTO();
        expectedResponse.setId(100L);
        expectedResponse.setName("Test Facility");
        expectedResponse.setImageUrl("http://minio/bucket/test.jpg");

        when(ownerService.createFacility(eq(ownerId), any(FacilityRequestDTO.class), any())).thenReturn(expectedResponse);

        // Act & Assert
        // OwnerController mapping: /api/owner/{ownerId}/facilities
        // Actually class @RequestMapping is /api/owner, method is /{ownerId}/facilities
        mockMvc.perform(multipart("/api/owner/{ownerId}/facilities", ownerId)
                .file(imageFile)
                .file(jsonFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("http://minio/bucket/test.jpg"));
    }


    @Test
    @WithMockUser(username = "owner@example.com", roles = {"OWNER"})
    void getOwnerFacilities_ShouldReturnList() throws Exception {
        Long ownerId = 1L;
        FacilityResponseDTO f1 = new FacilityResponseDTO();
        f1.setName("Gym A");
        
        when(ownerService.getFacilities(ownerId)).thenReturn(java.util.List.of(f1));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/owner/{ownerId}/facilities", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Gym A"));
    }

    @Test
    @WithMockUser(username = "owner@example.com", roles = {"OWNER"})
    void updateFacility_ShouldReturnUpdated() throws Exception {
        Long ownerId = 1L;
        Long facilityId = 100L;
        FacilityRequestDTO request = new FacilityRequestDTO();
        request.setName("Updated Gym");

        FacilityResponseDTO response = new FacilityResponseDTO();
        response.setName("Updated Gym");

        when(ownerService.updateFacility(eq(ownerId), eq(facilityId), any())).thenReturn(response);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/owner/{ownerId}/facilities/{facilityId}", ownerId, facilityId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Gym"));
    }

    @Test
    @WithMockUser(username = "owner@example.com", roles = {"OWNER"})
    void addEquipment_ShouldReturnCreated() throws Exception {
        Long ownerId = 1L;
        Long facilityId = 100L;
        tqs.sportslink.dto.EquipmentRequestDTO request = new tqs.sportslink.dto.EquipmentRequestDTO();
        request.setName("Ball");
        request.setType("Ball");
        request.setQuantity(10);
        request.setStatus("AVAILABLE");
        request.setPricePerHour(10.0);

        tqs.sportslink.dto.EquipmentResponseDTO response = new tqs.sportslink.dto.EquipmentResponseDTO();
        response.setName("Ball");

        when(ownerService.addEquipment(eq(ownerId), eq(facilityId), any())).thenReturn(response);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/owner/{ownerId}/facilities/{facilityId}/equipment", ownerId, facilityId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ball"));
    }
}
