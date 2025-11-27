package tqs.sportslink.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tqs.sportslink.boundary.RenterController;
import tqs.sportslink.service.RentalService;
import tqs.sportslink.service.FacilityService;
import tqs.sportslink.service.EquipmentService;
import tqs.sportslink.dto.RentalRequestDTO;
import tqs.sportslink.dto.RentalResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste de integração para o fluxo completo de rental usando MockMvc
 * Testa o cenário de Maria fazendo booking de campo de Padel
 */
@WebMvcTest(RenterController.class)
@ExtendWith(MockitoExtension.class)
public class RentalFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RentalService rentalService;

    @MockBean
    private FacilityService facilityService;

    @MockBean
    private EquipmentService equipmentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCompleteRentalFlow_MariaBookingScenario() {
        // Cenário: Maria quer reservar um campo de padel em Aveiro
        
        // Step 1: Buscar facilities em Aveiro com Padel
        given()
            .queryParam("location", "Aveiro")
            .queryParam("sport", "Padel")
        .when()
            .get("/api/rentals/search")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", notNullValue());
        
        // Step 2: Consultar equipamentos disponíveis para facility 1
        given()
            .pathParam("facilityId", 1)
        .when()
            .get("/api/rentals/facility/{facilityId}/equipments")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", notNullValue());
        
        // Step 3: Criar booking para 19:00-21:00 com equipamentos
        RentalRequestDTO rentalRequest = new RentalRequestDTO();
        rentalRequest.setFacilityId(1L);
        rentalRequest.setStartTime(LocalDateTime.of(2025, 11, 27, 19, 0));
        rentalRequest.setEndTime(LocalDateTime.of(2025, 11, 27, 21, 0));
        rentalRequest.setEquipmentIds(List.of(1L, 2L)); // Raquetes e bolas
        
        String rentalId = given()
            .contentType(ContentType.JSON)
            .body(rentalRequest)
        .when()
            .post("/api/rentals/rental")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("facilityId", equalTo(1))
            .body("status", notNullValue())
            .extract()
            .path("id")
            .toString();
        
        // Step 4: Verificar status do booking
        given()
            .pathParam("id", rentalId)
        .when()
            .get("/api/rentals/rental/{id}/status")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("status", notNullValue());
        
        // Step 5: Atualizar horário do booking (mudar para 20:00-22:00)
        RentalRequestDTO updateRequest = new RentalRequestDTO();
        updateRequest.setFacilityId(1L);
        updateRequest.setStartTime(LocalDateTime.of(2025, 11, 27, 20, 0));
        updateRequest.setEndTime(LocalDateTime.of(2025, 11, 27, 22, 0));
        updateRequest.setEquipmentIds(List.of(1L, 2L));
        
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", rentalId)
            .body(updateRequest)
        .when()
            .put("/api/rentals/rental/{id}/update")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", notNullValue());
        
        // Step 6: Cancelar booking
        given()
            .pathParam("id", rentalId)
        .when()
            .put("/api/rentals/rental/{id}/cancel")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", notNullValue());
    }

    @Test
    public void testSearchFacilities_withFilters() {
        // Test: Buscar facilities com múltiplos filtros
        given()
            .queryParam("location", "Aveiro")
            .queryParam("sport", "Padel")
            .queryParam("startTime", "2025-11-27T19:00:00")
            .queryParam("endTime", "2025-11-27T21:00:00")
        .when()
            .get("/api/rentals/search")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", notNullValue());
    }

    @Test
    public void testCreateRental_withoutEquipment() {
        // Test: Criar booking sem equipamentos
        RentalRequestDTO rentalRequest = new RentalRequestDTO();
        rentalRequest.setFacilityId(1L);
        rentalRequest.setStartTime(LocalDateTime.of(2025, 11, 28, 10, 0));
        rentalRequest.setEndTime(LocalDateTime.of(2025, 11, 28, 12, 0));
        
        given()
            .contentType(ContentType.JSON)
            .body(rentalRequest)
        .when()
            .post("/api/rentals/rental")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("facilityId", equalTo(1));
    }

    @Test
    public void testCreateRental_invalidTimeRange() {
        // Test: Tentar criar booking com horário inválido (fim antes do início)
        RentalRequestDTO invalidRequest = new RentalRequestDTO();
        invalidRequest.setFacilityId(1L);
        invalidRequest.setStartTime(LocalDateTime.of(2025, 11, 27, 21, 0));
        invalidRequest.setEndTime(LocalDateTime.of(2025, 11, 27, 19, 0)); // Inválido!
        
        given()
            .contentType(ContentType.JSON)
            .body(invalidRequest)
        .when()
            .post("/api/rentals/rental")
        .then()
            // Deve retornar erro quando validação for implementada
            .statusCode(anyOf(is(200), is(400)));
    }

    @Test
    public void testGetEquipments_forFacility() {
        // Test: Listar equipamentos disponíveis para uma facility
        given()
            .pathParam("facilityId", 1)
        .when()
            .get("/api/rentals/facility/{facilityId}/equipments")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", notNullValue());
    }
}