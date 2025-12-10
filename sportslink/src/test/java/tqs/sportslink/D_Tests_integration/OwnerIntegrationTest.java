package tqs.sportslink.D_Tests_integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import tqs.sportslink.data.EquipmentRepository;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Equipment;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Role;
import tqs.sportslink.data.model.Sport;
import tqs.sportslink.data.model.User;
import tqs.sportslink.dto.AuthResponseDTO;
import tqs.sportslink.dto.EquipmentRequestDTO;
import tqs.sportslink.dto.FacilityRequestDTO;
import tqs.sportslink.dto.UserRequestDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration") // segurança ativa (SecurityConfig carregada)
class OwnerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    private User ownerUser;
    private User renterUser;
    private Facility ownerFacility;

    private String ownerToken;
    private String renterToken;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        equipmentRepository.deleteAll();
        facilityRepository.deleteAll();
        userRepository.deleteAll();

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // OWNER
        User owner = new User();
        owner.setEmail("owner@example.com");
        owner.setPassword(encoder.encode("ownerpass"));
        owner.setName("Owner User");
        owner.getRoles().add(Role.OWNER);
        owner.setActive(true);
        ownerUser = userRepository.save(owner);

        // RENTER (para testar 403)
        User renter = new User();
        renter.setEmail("renter@example.com");
        renter.setPassword(encoder.encode("renterpass"));
        renter.setName("Renter User");
        renter.getRoles().add(Role.RENTER);
        renter.setActive(true);
        renterUser = userRepository.save(renter);

        // Facility do OWNER
        Facility facility = new Facility();
        facility.setName("Owner Test Facility");
        facility.setSports(List.of(Sport.FOOTBALL));
        facility.setCity("Porto");
        facility.setAddress("Rua do Owner 123");
        facility.setPricePerHour(20.0);
        facility.setStatus("ACTIVE");
        facility.setOwner(ownerUser);
        ownerFacility = facilityRepository.save(facility);

        // LOGIN OWNER
        UserRequestDTO ownerLogin = new UserRequestDTO();
        ownerLogin.setEmail("owner@example.com");
        ownerLogin.setPassword("ownerpass");

        ownerToken = given()
                .contentType(ContentType.JSON)
                .body(ownerLogin)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(200)
                .extract()
                .as(AuthResponseDTO.class)
                .getToken();

        // LOGIN RENTER
        UserRequestDTO renterLogin = new UserRequestDTO();
        renterLogin.setEmail("renter@example.com");
        renterLogin.setPassword("renterpass");

        renterToken = given()
                .contentType(ContentType.JSON)
                .body(renterLogin)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(200)
                .extract()
                .as(AuthResponseDTO.class)
                .getToken();
    }

    @Test
    void whenOwnerGetFacilities_thenSuccess() {
        given()
            .header("Authorization", "Bearer " + ownerToken)
        .when()
            .get("/api/owner/" + ownerUser.getId() + "/facilities")
        .then()
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].name", equalTo("Owner Test Facility"))
            .body("[0].city", equalTo("Porto"));
    }

    @Test
    void whenOwnerUpdateFacility_thenSuccess() {
        FacilityRequestDTO update = new FacilityRequestDTO();
        update.setName("Updated Facility");
        update.setSports(List.of(Sport.FOOTBALL));
        update.setCity("Lisboa");
        update.setAddress("Nova Morada 456");
        update.setDescription("Descrição atualizada");
        update.setPricePerHour(30.0);
        update.setOpeningTime("08:00");
        update.setClosingTime("23:00");

        given()
            .header("Authorization", "Bearer " + ownerToken)
            .contentType(ContentType.JSON)
            .body(update)
        .when()
            .put("/api/owner/" + ownerUser.getId() + "/facilities/" + ownerFacility.getId())
        .then()
            .statusCode(200)
            .body("id", equalTo(ownerFacility.getId().intValue()))
            .body("name", equalTo("Updated Facility"))
            .body("city", equalTo("Lisboa"))
            .body("pricePerHour", equalTo(30.0f))
            .body("openingTime", equalTo("08:00"))
            .body("closingTime", equalTo("23:00"));
    }

    @Test
    void whenOwnerDeleteFacility_thenItIsNotListedAnymore() {
        given()
            .header("Authorization", "Bearer " + ownerToken)
        .when()
            .delete("/api/owner/" + ownerUser.getId() + "/facilities/" + ownerFacility.getId())
        .then()
            .statusCode(204);

        given()
            .header("Authorization", "Bearer " + ownerToken)
        .when()
            .get("/api/owner/" + ownerUser.getId() + "/facilities")
        .then()
            .statusCode(200)
            .body("$", hasSize(0));
    }

    @Test
    void whenOwnerAddAndListEquipment_thenSuccess() {
        EquipmentRequestDTO equipmentDto = new EquipmentRequestDTO();
        equipmentDto.setName("Padel Racket");
        equipmentDto.setType("Racket");
        equipmentDto.setDescription("Raquete profissional");
        equipmentDto.setQuantity(5);
        equipmentDto.setPricePerHour(3.5);
        equipmentDto.setStatus("AVAILABLE");

        Integer equipmentIdInt = given()
            .header("Authorization", "Bearer " + ownerToken)
            .contentType(ContentType.JSON)
            .body(equipmentDto)
        .when()
            .post("/api/owner/" + ownerUser.getId() + "/facilities/" + ownerFacility.getId() + "/equipment")
        .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("name", equalTo("Padel Racket"))
            .body("status", equalTo("AVAILABLE"))
        .extract()
            .path("id");

        given()
            .header("Authorization", "Bearer " + ownerToken)
        .when()
            .get("/api/owner/" + ownerUser.getId() + "/facilities/" + ownerFacility.getId() + "/equipment")
        .then()
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].id", equalTo(equipmentIdInt))
            .body("[0].name", equalTo("Padel Racket"));
    }

    @Test
    void whenOwnerUpdateEquipment_thenSuccess() {
        Equipment equipment = new Equipment();
        equipment.setName("Old Ball");
        equipment.setType("Ball");
        equipment.setDescription("Bola antiga");
        equipment.setQuantity(10);
        equipment.setPricePerHour(2.0);
        equipment.setStatus("AVAILABLE");
        equipment.setFacility(ownerFacility);
        equipment = equipmentRepository.save(equipment);

        EquipmentRequestDTO updateDto = new EquipmentRequestDTO();
        updateDto.setName("New Ball");
        updateDto.setType("Ball");
        updateDto.setDescription("Bola nova oficial");
        updateDto.setQuantity(20);
        updateDto.setPricePerHour(2.5);
        updateDto.setStatus("MAINTENANCE");

        given()
            .header("Authorization", "Bearer " + ownerToken)
            .contentType(ContentType.JSON)
            .body(updateDto)
        .when()
            .put("/api/owner/" + ownerUser.getId() + "/equipment/" + equipment.getId())
        .then()
            .statusCode(200)
            .body("id", equalTo(equipment.getId().intValue()))
            .body("name", equalTo("New Ball"))
            .body("quantity", equalTo(20))
            .body("pricePerHour", equalTo(2.5f))
            .body("status", equalTo("MAINTENANCE"));
    }

    @Test
    void whenRenterTriesOwnerEndpoint_thenForbidden() {
        given()
            .header("Authorization", "Bearer " + renterToken)
        .when()
            .get("/api/owner/" + ownerUser.getId() + "/facilities")
        .then()
            .statusCode(403);
    }
}
