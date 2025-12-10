package tqs.sportslink.D_Tests_integration;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Role;
import tqs.sportslink.data.model.Sport;
import tqs.sportslink.data.model.User;

import tqs.sportslink.config.TestSecurityConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Requirement("SL-343")
@org.springframework.context.annotation.Import(TestSecurityConfig.class)
public class AdminIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @BeforeEach
    void setup() {
        RestAssured.port = port;

        // Clean DB
        facilityRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create Admin User (just for data consistency, auth is disabled)
        User admin = new User();
        admin.setEmail("admin@admin.com");
        admin.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("adminpass"));
        admin.setName("Admin");
        admin.getRoles().add(Role.ADMIN);
        admin.setActive(true);
        userRepository.save(admin);

        // 2. Create Renter User
        User renter = new User();
        renter.setEmail("renter@user.com");
        renter.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("renterpass"));
        renter.setName("Renter");
        renter.getRoles().add(Role.RENTER);
        renter.setActive(true);
        userRepository.save(renter);

        // 3. Create Sample Facility
        Facility f = new Facility();
        f.setName("Admin Test Facility");
        f.setAddress("Test St");
        f.setCity("Test City");
        f.setPricePerHour(10.0);
        f.setStatus("ACTIVE");
        f.setSports(List.of(Sport.FOOTBALL));
        facilityRepository.save(f);
    }

    @Test
    void whenAdminGetUsers_thenSuccess() {
        given()
                .when()
                .get("/api/admin/users")
                .then()
                .statusCode(200)
                .body("$", hasSize(2)); // Admin + Renter
    }

    @Test
    void whenAdminGetFacilities_thenSuccess() {
        given()
                .when()
                .get("/api/admin/facilities")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("Admin Test Facility"));
    }

    @Test
    void whenAdminGetStats_thenSuccess() {
        given()
                .when()
                .get("/api/admin/stats")
                .then()
                .statusCode(200)
                .body("totalUsers", equalTo(2))
                .body("totalFacilities", equalTo(1));
    }

    @Test
    void whenAdminBanUser_thenUserIsInactive() {
        // 1. Get the Renter user
        User renter = userRepository.findByEmail("renter@user.com").orElseThrow();
        Long renterId = renter.getId();

        // 2. Ban the user (active = false)
        given()
                .queryParam("active", false)
                .when()
                .put("/api/admin/users/" + renterId + "/status")
                .then()
                .statusCode(200)
                .body("active", equalTo(false));

        // Verify in DB
        renter = userRepository.findById(renterId).orElseThrow();
        assertFalse(renter.getActive());

        // 3. Unban the user (active = true)
        given()
                .queryParam("active", true)
                .when()
                .put("/api/admin/users/" + renterId + "/status")
                .then()
                .statusCode(200)
                .body("active", equalTo(true));

        // Verify in DB
        renter = userRepository.findById(renterId).orElseThrow();
        assertTrue(renter.getActive());
    }
}
