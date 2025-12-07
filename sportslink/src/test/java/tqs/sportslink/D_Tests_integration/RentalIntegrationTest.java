package tqs.sportslink.D_Tests_integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import tqs.sportslink.data.EquipmentRepository;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.RentalRepository;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Equipment;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Role;
import tqs.sportslink.data.model.Sport;
import tqs.sportslink.data.model.User;
import tqs.sportslink.dto.RentalRequestDTO;

/**
 * INTEGRATION TEST REAL usando RestAssured - SEM MOCKS
 * Testa API REST completa: Controller → Service → Repository → Database (H2)
 * 
 * Cenário: Maria busca facilities em Aveiro para Padel às 19:00-21:00 
 * e faz booking completo com equipamentos
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RentalIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private UserRepository userRepository;

    private Facility testFacility;
    private Equipment testEquipment;
    private User testUser;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Limpar base de dados H2
        rentalRepository.deleteAll();
        equipmentRepository.deleteAll();
        facilityRepository.deleteAll();
        userRepository.deleteAll();

        // Criar user de teste (Maria)
        testUser = new User();
        testUser.setEmail("maria@example.com");
        testUser.setName("Maria Silva");
        testUser.setPassword("encoded-password");
        testUser.getRoles().add(Role.RENTER);
        testUser.setActive(true);
        testUser = userRepository.save(testUser);

        // Criar facility de teste (Padel em Aveiro)
        testFacility = new Facility();
        testFacility.setName("Padel Club Aveiro");
        testFacility.setSports(List.of(Sport.PADEL));
        testFacility.setCity("Aveiro");
        testFacility.setAddress("Rua do Padel, 123");
        testFacility.setPricePerHour(15.0);
        testFacility.setStatus("ACTIVE");
        testFacility = facilityRepository.save(testFacility);

        // Criar equipamento de teste
        testEquipment = new Equipment();
        testEquipment.setName("Raquete Profissional");
        testEquipment.setType("Racket");
        testEquipment.setQuantity(10);
        testEquipment.setPricePerHour(5.0);
        testEquipment.setStatus("AVAILABLE");
        testEquipment.setFacility(testFacility);
        testEquipment = equipmentRepository.save(testEquipment);
    }

    /**
     * Teste do fluxo completo: buscar -> consultar equipamentos -> criar rental -> atualizar -> cancelar
     */
    @Test
    void whenCompleteRentalFlow_thenSuccess() {
        // STEP 1: Maria busca facilities em Aveiro para Padel
        given()
            .queryParam("location", "Aveiro")
            .queryParam("sport", "Padel")
            .queryParam("time", "19:00")
        .when()
            .get("/api/rentals/search")
        .then()
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].name", equalTo("Padel Club Aveiro"));

        // STEP 2: Maria consulta equipamentos disponíveis
        given()
        .when()
            .get("/api/rentals/facility/" + testFacility.getId() + "/equipments")
        .then()
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].name", equalTo("Raquete Profissional"));

        // STEP 3: Maria cria booking com equipamento
        RentalRequestDTO request = new RentalRequestDTO();
        request.setUserId(testUser.getId());
        request.setFacilityId(testFacility.getId());
        request.setStartTime(LocalDateTime.now().plusDays(1).withHour(19).withMinute(0).withSecond(0).withNano(0));
        request.setEndTime(LocalDateTime.now().plusDays(1).withHour(21).withMinute(0).withSecond(0).withNano(0));
        request.setEquipmentIds(List.of(testEquipment.getId()));

        Integer rentalIdInt = given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/rentals/rental")
        .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("status", equalTo("CONFIRMED"))
            .body("facilityId", equalTo(testFacility.getId().intValue()))
            .body("equipments", hasSize(1))
            .body("equipments[0]", equalTo("Raquete Profissional"))
        .extract()
            .path("id");
        Long rentalId = rentalIdInt.longValue();

        // STEP 4: Maria consulta status do booking
        given()
        .when()
            .get("/api/rentals/rental/" + rentalId + "/status")
        .then()
            .statusCode(200)
            .body("id", equalTo(rentalId.intValue()))
            .body("status", equalTo("CONFIRMED"));

        // STEP 5: Maria atualiza horário (20:00-22:00)
        RentalRequestDTO updateRequest = new RentalRequestDTO();
        updateRequest.setUserId(testUser.getId());
        updateRequest.setFacilityId(testFacility.getId());
        updateRequest.setStartTime(LocalDateTime.now().plusDays(1).withHour(20).withMinute(0).withSecond(0).withNano(0));
        updateRequest.setEndTime(LocalDateTime.now().plusDays(1).withHour(22).withMinute(0).withSecond(0).withNano(0));

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/rentals/rental/" + rentalId + "/update")
        .then()
            .statusCode(200)
            .body("status", equalTo("CONFIRMED"));

        // STEP 6: Maria cancela booking
        given()
        .when()
            .put("/api/rentals/rental/" + rentalId + "/cancel")
        .then()
            .statusCode(200)
            .body("status", equalTo("CANCELLED"));
    }

    /**
     * Teste de detecção de conflito ao criar dois rentals no mesmo horário
     */
    @Test
    void whenCreateConflictingRental_thenStatus400() {
        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = LocalDateTime.now().plusDays(2).withHour(16).withMinute(0).withSecond(0).withNano(0);

        // Criar primeiro rental
        RentalRequestDTO request1 = new RentalRequestDTO();
        request1.setUserId(testUser.getId());
        request1.setFacilityId(testFacility.getId());
        request1.setStartTime(start);
        request1.setEndTime(end);

        given()
            .contentType(ContentType.JSON)
            .body(request1)
        .when()
            .post("/api/rentals/rental")
        .then()
            .statusCode(200);

        // Tentar criar segundo rental no mesmo horário
        RentalRequestDTO request2 = new RentalRequestDTO();
        request2.setUserId(testUser.getId());
        request2.setFacilityId(testFacility.getId());
        request2.setStartTime(start);
        request2.setEndTime(end);

        given()
            .contentType(ContentType.JSON)
            .body(request2)
        .when()
            .post("/api/rentals/rental")
        .then()
            .statusCode(400);
    }

    /**
     * Teste de criação de rental com facility inexistente
     */
    @Test
    void whenCreateRentalWithInvalidFacility_thenStatus400() {
        RentalRequestDTO request = new RentalRequestDTO();
        request.setUserId(testUser.getId());
        request.setFacilityId(9999L); // Facility inexistente
        request.setStartTime(LocalDateTime.now().plusDays(1).withHour(19).withMinute(0).withSecond(0).withNano(0));
        request.setEndTime(LocalDateTime.now().plusDays(1).withHour(21).withMinute(0).withSecond(0).withNano(0));

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/rentals/rental")
        .then()
            .statusCode(400);
    }

    /**
     * Teste de criação de rental no passado
     */
    @Test
    void whenCreateRentalInPast_thenStatus400() {
        RentalRequestDTO request = new RentalRequestDTO();
        request.setUserId(testUser.getId());
        request.setFacilityId(testFacility.getId());
        request.setStartTime(LocalDateTime.now().minusDays(1));
        request.setEndTime(LocalDateTime.now().minusDays(1).plusHours(2));

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/rentals/rental")
        .then()
            .statusCode(400);
    }

    /**
     * Teste de cancelamento de rental inexistente
     */
    @Test
    void whenCancelInvalidRental_thenStatus400() {
        given()
        .when()
            .put("/api/rentals/rental/99999/cancel")
        .then()
            .statusCode(400);
    }

    /**
     * Teste de update causando conflito com outro rental existente
     */
    @Test
    void whenUpdateRentalCausesConflict_thenStatus400() {
        // Criar primeiro rental 14:00-16:00
        RentalRequestDTO request1 = new RentalRequestDTO();
        request1.setUserId(testUser.getId());
        request1.setFacilityId(testFacility.getId());
        request1.setStartTime(LocalDateTime.now().plusDays(20).withHour(14).withMinute(0).withSecond(0).withNano(0));
        request1.setEndTime(LocalDateTime.now().plusDays(20).withHour(16).withMinute(0).withSecond(0).withNano(0));

        Integer rentalId1Int = given()
            .contentType(ContentType.JSON)
            .body(request1)
        .when()
            .post("/api/rentals/rental")
        .then()
            .statusCode(200)
        .extract()
            .path("id");
        Long rentalId1 = rentalId1Int.longValue();

        // Criar segundo rental 17:00-19:00 (sem conflito com o primeiro)
        RentalRequestDTO request2 = new RentalRequestDTO();
        request2.setUserId(testUser.getId());
        request2.setFacilityId(testFacility.getId());
        request2.setStartTime(LocalDateTime.now().plusDays(20).withHour(17).withMinute(0).withSecond(0).withNano(0));
        request2.setEndTime(LocalDateTime.now().plusDays(20).withHour(19).withMinute(0).withSecond(0).withNano(0));

        given()
            .contentType(ContentType.JSON)
            .body(request2)
        .when()
            .post("/api/rentals/rental")
        .then()
            .statusCode(200);

        // Tentar atualizar primeiro rental para 17:00-19:00 (conflito com segundo)
        RentalRequestDTO updateRequest = new RentalRequestDTO();
        updateRequest.setUserId(testUser.getId());
        updateRequest.setFacilityId(testFacility.getId());
        updateRequest.setStartTime(LocalDateTime.now().plusDays(20).withHour(17).withMinute(0).withSecond(0).withNano(0));
        updateRequest.setEndTime(LocalDateTime.now().plusDays(20).withHour(19).withMinute(0).withSecond(0).withNano(0));

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/rentals/rental/" + rentalId1 + "/update")
        .then()
            .statusCode(400);
    }

    /**
     * Teste de filtro de equipamentos AVAILABLE
     */
    @Test
    void whenGetEquipments_thenOnlyAvailableReturned() {
        // Criar equipamento UNAVAILABLE
        Equipment unavailable = new Equipment();
        unavailable.setName("Bola Furada");
        unavailable.setType("Ball");
        unavailable.setQuantity(0);
        unavailable.setPricePerHour(3.0);
        unavailable.setStatus("UNAVAILABLE");
        unavailable.setFacility(testFacility);
        equipmentRepository.save(unavailable);

        // Deve retornar apenas AVAILABLE
        given()
        .when()
            .get("/api/rentals/facility/" + testFacility.getId() + "/equipments")
        .then()
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].name", equalTo("Raquete Profissional"));
    }
}
