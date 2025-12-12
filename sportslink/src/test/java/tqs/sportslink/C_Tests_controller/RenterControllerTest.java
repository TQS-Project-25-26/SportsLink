package tqs.sportslink.C_Tests_controller;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tqs.sportslink.boundary.RenterController;
import tqs.sportslink.config.TestSecurityConfig;
import tqs.sportslink.dto.EquipmentResponseDTO;
import tqs.sportslink.dto.FacilityResponseDTO;
import tqs.sportslink.dto.RentalRequestDTO;
import tqs.sportslink.dto.RentalResponseDTO;
import tqs.sportslink.data.model.Sport;
import tqs.sportslink.service.EquipmentService;
import tqs.sportslink.service.FacilityService;
import tqs.sportslink.service.RentalService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RenterController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@Requirement("SL-27")
class RenterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RentalService rentalService;

    @MockitoBean
    private FacilityService facilityService;

    @MockitoBean
    private EquipmentService equipmentService;

    @Test
    @Requirement("SL-27")
    void whenSearchFacilities_thenReturnList() throws Exception {
        // Given
        FacilityResponseDTO dto1 = new FacilityResponseDTO();
        dto1.setId(1L);
        dto1.setName("Padel Club Aveiro");
        dto1.setSports(List.of(Sport.PADEL));
        dto1.setCity("Aveiro");

        List<FacilityResponseDTO> facilities = List.of(dto1);
        when(facilityService.searchFacilities("Aveiro", "Padel", "19:00")).thenReturn(facilities);

        // When & Then
        mockMvc.perform(get("/api/rentals/search")
                .param("location", "Aveiro")
                .param("sport", "Padel")
                .param("time", "19:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Padel Club Aveiro")));
    }

    @Test
    @Requirement("SL-26")
    void whenCreateRental_thenReturnRental() throws Exception {
        // Given
        RentalRequestDTO request = new RentalRequestDTO();
        request.setUserId(1L);
        request.setFacilityId(1L);
        request.setStartTime(LocalDateTime.of(2025, 12, 27, 19, 0));
        request.setEndTime(LocalDateTime.of(2025, 12, 27, 21, 0));

        RentalResponseDTO response = new RentalResponseDTO();
        response.setId(1L);
        response.setStatus("CONFIRMED");

        when(rentalService.createRental(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/rentals/rental")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("CONFIRMED")));
    }

    @Test
    void test_getEquipments_returns200_validFacilityId() throws Exception {
        // Given
        EquipmentResponseDTO eq1 = new EquipmentResponseDTO();
        eq1.setId(1L);
        eq1.setName("Bola");

        when(equipmentService.getEquipmentsByFacility(1L)).thenReturn(List.of(eq1));

        // When & Then
        mockMvc.perform(get("/api/rentals/facility/1/equipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Bola")));
    }

    @Test
    @Requirement("SL-30")
    void test_cancelRental_returns200_validId() throws Exception {
        // Given
        RentalResponseDTO response = new RentalResponseDTO();
        response.setId(1L);
        response.setStatus("CANCELLED");

        when(rentalService.cancelRental(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/rentals/rental/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @Requirement("SL-30")
    void test_updateRental_returns200_validChanges() throws Exception {
        // Given
        RentalRequestDTO request = new RentalRequestDTO();
        request.setUserId(1L);
        request.setFacilityId(1L);
        // Fixed dates for stability
        request.setStartTime(LocalDateTime.of(2025, 12, 28, 20, 0));
        request.setEndTime(LocalDateTime.of(2025, 12, 28, 22, 0));

        RentalResponseDTO response = new RentalResponseDTO();
        response.setId(1L);
        response.setStatus("UPDATED");

        when(rentalService.updateRental(org.mockito.ArgumentMatchers.eq(1L), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/rentals/rental/1/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UPDATED")));
    }

    @Test
    @Requirement("SL-30")
    void test_getRentalStatus_returns200_validId() throws Exception {
        // Given
        RentalResponseDTO response = new RentalResponseDTO();
        response.setId(1L);
        response.setStatus("ACTIVE");

        when(rentalService.getRentalStatus(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/rentals/rental/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    @Requirement("SL-30")
    void test_getUserHistory_returns200() throws Exception {
        // Given
        RentalResponseDTO r1 = new RentalResponseDTO();
        r1.setId(10L);
        r1.setStatus("CONFIRMED");

        RentalResponseDTO r2 = new RentalResponseDTO();
        r2.setId(11L);
        r2.setStatus("CANCELLED");

        when(rentalService.getUserRentals(1L)).thenReturn(List.of(r1, r2));

        // When & Then
        mockMvc.perform(get("/api/rentals/history").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(10)))
                .andExpect(jsonPath("$[1].status", is("CANCELLED")));
    }
}
