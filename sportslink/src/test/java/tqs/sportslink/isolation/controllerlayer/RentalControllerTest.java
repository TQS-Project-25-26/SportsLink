package tqs.sportslink.isolation.controllerlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tqs.sportslink.service.RentalService;
import tqs.sportslink.service.FacilityService;
import tqs.sportslink.service.EquipmentService;
import tqs.sportslink.dto.RentalRequestDTO;
import tqs.sportslink.dto.RentalResponseDTO;

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

    @MockBean
    private RentalService rentalService;

    @MockBean
    private FacilityService facilityService;

    @MockBean
    private EquipmentService equipmentService;

    @Test
    public void test_searchFacilities_returns200_validParams() throws Exception {
        // Given - Maria's search scenario
        List<String> facilities = List.of("Padel Club Aveiro", "Sports Center", "Academy Pro");
        when(facilityService.searchFacilities("Aveiro", "Padel", "19:00")).thenReturn(facilities);

        // When & Then
        mockMvc.perform(get("/api/rentals/search")
                .param("location", "Aveiro")
                .param("sport", "Padel")
                .param("time", "19:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]", is("Padel Club Aveiro")));
    }

    @Test
    public void test_searchFacilities_returns200_emptyList_noMatches() throws Exception {
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
    public void test_createRental_returns200_validRequest() throws Exception {
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
    public void test_cancelRental_returns200_validId() throws Exception {
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
    public void test_updateRental_returns200_validChanges() throws Exception {
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
    public void test_getRentalStatus_returns200_validId() throws Exception {
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
    public void test_getEquipments_returns200_validFacilityId() throws Exception {
        // Given - Maria checking equipment
        List<String> equipments = List.of("Bola", "Raquete", "Rede");
        when(equipmentService.getEquipmentsByFacility(1L)).thenReturn(equipments);

        // When & Then
        mockMvc.perform(get("/api/rentals/facility/1/equipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]", is("Bola")));
    }
}
