package tqs.sportslink.D_Tests_integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Role;
import tqs.sportslink.data.model.Sport;
import tqs.sportslink.data.model.User;
import tqs.sportslink.dto.AuthResponseDTO;
import tqs.sportslink.dto.UserRequestDTO;

import java.time.LocalTime;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
public class AdminIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    private String adminToken;
    private String renterToken;

    @BeforeEach
    void setup() {
        RestAssured.port = port;

        // Clean DB
        facilityRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create Admin User
        User admin = new User();
        admin.setEmail("admin@admin.com");
        admin.setPassword("adminpass"); // Will be encoded by service logic usually, but here we save direct?
        // Wait, integration test uses real service?
        // If I save directly to Repo, I must encode password if the auth service
        // expects encoded.
        // authService.login() checks matches(raw, encoded).
        // I should inject PasswordEncoder or use AuthService to register.
        // But AuthService.register doesn't allow creating ADMIN directly easily (config
        // logic).
        // I will use BCrypt to save to DB.
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
        f.setSports(List.of(Sport.FOOTBALL)); // Ensure sports list is set
        facilityRepository.save(f);

        // 4. Authenticate to get Tokens
        // Login Admin
        UserRequestDTO adminLogin = new UserRequestDTO();
        adminLogin.setEmail("admin@admin.com");
        adminLogin.setPassword("adminpass");

        adminToken = given()
                .contentType(ContentType.JSON)
                .body(adminLogin)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract().as(AuthResponseDTO.class).getToken();

        // Login Renter
        UserRequestDTO renterLogin = new UserRequestDTO();
        renterLogin.setEmail("renter@user.com");
        renterLogin.setPassword("renterpass");

        renterToken = given()
                .contentType(ContentType.JSON)
                .body(renterLogin)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract().as(AuthResponseDTO.class).getToken();
    }

    @Test
    void whenAdminGetUsers_thenSuccess() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/admin/users")
                .then()
                .statusCode(200)
                .body("$", hasSize(2)); // Admin + Renter
    }

    @Test
    void whenAdminGetFacilities_thenSuccess() {
        given()
                .header("Authorization", "Bearer " + adminToken)
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
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/admin/stats")
                .then()
                .statusCode(200)
                .body("totalUsers", equalTo(2))
                .body("totalFacilities", equalTo(1));
    }

    @Test
    void whenRenterTryAdminEndpoint_thenForbidden() {
        given()
                .header("Authorization", "Bearer " + renterToken)
                .when()
                .get("/api/admin/users")
                .then()
                .statusCode(403);
    }
}
