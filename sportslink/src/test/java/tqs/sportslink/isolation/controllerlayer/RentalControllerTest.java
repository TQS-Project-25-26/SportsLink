package tqs.sportslink.isolation.controllerlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tqs.sportslink.service.RentalService;
import tqs.sportslink.service.FacilityService;
import tqs.sportslink.service.EquipmentService;
import tqs.sportslink.dto.RentalRequestDTO;
import tqs.sportslink.dto.RentalResponseDTO;
import tqs.sportslink.dto.FacilityResponseDTO;
import tqs.sportslink.data.model.Sport;
import tqs.sportslink.dto.EquipmentResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class RentalControllerTest {

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
    void test_searchFacilities_returns200_validParams() throws Exception {
        // Given - Maria's search scenario
        FacilityResponseDTO dto1 = new FacilityResponseDTO();
        dto1.setId(1L);
        dto1.setName("Padel Club Aveiro");
        dto1.setSports(List.of(Sport.PADEL));
        dto1.setCity("Aveiro");
        
        FacilityResponseDTO dto2 = new FacilityResponseDTO();
        dto2.setId(2L);
        dto2.setName("Sports Center");
        
        FacilityResponseDTO dto3 = new FacilityResponseDTO();
        dto3.setId(3L);
        dto3.setName("Academy Pro");
        
        List<FacilityResponseDTO> facilities = List.of(dto1, dto2, dto3);
        when(facilityService.searchFacilities("Aveiro", "Padel", "19:00")).thenReturn(facilities);

        // When & Then
        mockMvc.perform(get("/api/rentals/search")
                .param("location", "Aveiro")
                .param("sport", "Padel")
                .param("time", "19:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name", is("Padel Club Aveiro")));
    }

    @Test
    void test_searchFacilities_returns200_emptyList_noMatches() throws Exception {
        // Given
        when(facilityService.searchFacilities("Porto", "Tennis", "02:00")).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/rentals/search")
                .param("location", "Porto")
                .param("sport", "Tennis")
                .param("time", "02:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void test_createRental_returns200_validRequest() throws Exception {
        // Given - Maria booking Padel Club Aveiro
        RentalRequestDTO request = new RentalRequestDTO();
        request.setUserId(1L);
        request.setFacilityId(1L);
        request.setStartTime(LocalDateTime.of(2025, 12, 27, 19, 0));
        request.setEndTime(LocalDateTime.of(2025, 12, 27, 21, 0));
        
        RentalResponseDTO response = new RentalResponseDTO();
        response.setId(1L);
        response.setFacilityId(1L);
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
    void test_updateRental_returns200_validChanges() throws Exception {
        // Given
        RentalRequestDTO request = new RentalRequestDTO();
        request.setUserId(1L);
        request.setFacilityId(1L);
        request.setStartTime(LocalDateTime.of(2025, 12, 27, 20, 0));
        request.setEndTime(LocalDateTime.of(2025, 12, 27, 22, 0));
        
        RentalResponseDTO response = new RentalResponseDTO();
        response.setId(1L);
        response.setStatus("UPDATED");
        
        when(rentalService.updateRental(any(), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/rentals/rental/1/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UPDATED")));
    }

    @Test
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
    void test_getEquipments_returns200_validFacilityId() throws Exception {
        // Given - Maria checking equipment
        EquipmentResponseDTO eq1 = new EquipmentResponseDTO();
        eq1.setId(1L);
        eq1.setName("Bola");
        
        EquipmentResponseDTO eq2 = new EquipmentResponseDTO();
        eq2.setId(2L);
        eq2.setName("Raquete");
        
        EquipmentResponseDTO eq3 = new EquipmentResponseDTO();
        eq3.setId(3L);
        eq3.setName("Rede");
        
        List<EquipmentResponseDTO> equipments = List.of(eq1, eq2, eq3);
        when(equipmentService.getEquipmentsByFacility(1L)).thenReturn(equipments);

        // When & Then
        mockMvc.perform(get("/api/rentals/facility/1/equipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name", is("Bola")));
    }
}
