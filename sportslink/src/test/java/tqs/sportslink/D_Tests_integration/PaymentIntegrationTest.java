package tqs.sportslink.D_Tests_integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.PaymentRepository;
import tqs.sportslink.data.RentalRepository;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Payment;
import tqs.sportslink.data.model.Rental;
import tqs.sportslink.data.model.Role;
import tqs.sportslink.data.model.Sport;
import tqs.sportslink.data.model.User;
import tqs.sportslink.dto.RentalRequestDTO;

/**
 * INTEGRATION TEST for Payment functionality
 * Tests the complete payment flow: Rental -> Payment creation -> Status
 * retrieval
 * 
 * Note: Actual Stripe API calls are not made in tests - this tests the database
 * layer and payment record management independently of Stripe.
 */
import tqs.sportslink.config.TestSecurityConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestSecurityConfig.class)
class PaymentIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    private Facility testFacility;
    private User testUser;
    private Rental testRental;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Clean database
        paymentRepository.deleteAll();
        rentalRepository.deleteAll();
        facilityRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setEmail("payer@example.com");
        testUser.setName("Payment Tester");
        testUser.setPassword("encoded-password");
        testUser.getRoles().add(Role.RENTER);
        testUser.setActive(true);
        testUser = userRepository.save(testUser);

        // Create test facility
        testFacility = new Facility();
        testFacility.setName("Payment Test Arena");
        testFacility.setSports(List.of(Sport.PADEL));
        testFacility.setCity("Aveiro");
        testFacility.setAddress("Rua de Teste, 123");
        testFacility.setPricePerHour(20.0);
        testFacility.setStatus("ACTIVE");
        testFacility.setOpeningTime(LocalTime.of(8, 0));
        testFacility.setClosingTime(LocalTime.of(22, 0));
        testFacility = facilityRepository.save(testFacility);
    }

    /**
     * Test: Get payment status for a non-existent rental
     */
    @Test
    void whenGetPaymentStatus_noPaymentExists_thenReturns404() {
        given()
                .when()
                .get("/api/payments/status/99999")
                .then()
                .statusCode(404);
    }

    /**
     * Test: Get Stripe config endpoint returns publishable key
     */
    @Test
    void whenGetStripeConfig_thenReturnsPublishableKey() {
        given()
                .when()
                .get("/api/payments/config")
                .then()
                .statusCode(200)
                .body("publishableKey", notNullValue());
    }

    /**
     * Test: Payment record is created with correct default values
     */
    @Test
    void whenPaymentCreated_thenDefaultValuesAreCorrect() {
        // Create rental first
        testRental = new Rental();
        testRental.setUser(testUser);
        testRental.setFacility(testFacility);
        testRental.setStatus("CONFIRMED");
        testRental.setTotalPrice(40.0);
        testRental.setPaymentStatus("UNPAID");
        testRental.setStartTime(LocalDateTime.now().plusDays(2));
        testRental.setEndTime(LocalDateTime.now().plusDays(2).plusHours(2));
        testRental = rentalRepository.save(testRental);

        // Create payment directly in repository
        Payment payment = new Payment(testRental, "pi_integration_test", 40.0, "payer@example.com");
        payment = paymentRepository.save(payment);

        // Verify default values
        assertThat(payment.getId()).isNotNull();
        assertThat(payment.getStatus()).isEqualTo("PENDING");
        assertThat(payment.getCurrency()).isEqualTo("EUR");
        assertThat(payment.getAmount()).isEqualTo(40.0);
        assertThat(payment.getCustomerEmail()).isEqualTo("payer@example.com");
        assertThat(payment.getRental().getId()).isEqualTo(testRental.getId());
    }

    /**
     * Test: Payment can be found by Stripe PaymentIntent ID
     */
    @Test
    void whenFindByPaymentIntentId_thenReturnsPayment() {
        // Setup
        testRental = new Rental();
        testRental.setUser(testUser);
        testRental.setFacility(testFacility);
        testRental.setStatus("CONFIRMED");
        testRental.setTotalPrice(30.0);
        testRental.setPaymentStatus("PENDING");
        testRental.setStartTime(LocalDateTime.now().plusDays(3));
        testRental.setEndTime(LocalDateTime.now().plusDays(3).plusHours(1));
        testRental = rentalRepository.save(testRental);

        Payment payment = new Payment(testRental, "pi_unique_intent_id", 30.0, "test@example.com");
        paymentRepository.save(payment);

        // Find
        var found = paymentRepository.findByStripePaymentIntentId("pi_unique_intent_id");

        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualTo(30.0);
    }

    /**
     * Test: Payment status can be updated
     */
    @Test
    void whenPaymentStatusUpdated_thenChangesPersist() {
        // Setup
        testRental = new Rental();
        testRental.setUser(testUser);
        testRental.setFacility(testFacility);
        testRental.setStatus("CONFIRMED");
        testRental.setTotalPrice(25.0);
        testRental.setPaymentStatus("PENDING");
        testRental.setStartTime(LocalDateTime.now().plusDays(4));
        testRental.setEndTime(LocalDateTime.now().plusDays(4).plusHours(1));
        testRental = rentalRepository.save(testRental);

        Payment payment = new Payment(testRental, "pi_status_update_test", 25.0, "update@example.com");
        payment = paymentRepository.save(payment);

        // Update status
        payment.setStatus("SUCCEEDED");
        payment.setStripeChargeId("ch_test_charge_id");
        payment.setReceiptUrl("https://receipt.stripe.com/test123");
        paymentRepository.save(payment);

        // Verify update persisted
        var found = paymentRepository.findById(payment.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo("SUCCEEDED");
        assertThat(found.get().getStripeChargeId()).isEqualTo("ch_test_charge_id");
        assertThat(found.get().getReceiptUrl()).isEqualTo("https://receipt.stripe.com/test123");
    }

    /**
     * Test: Failed payment stores failure message
     */
    @Test
    void whenPaymentFails_thenFailureMessageIsSaved() {
        // Setup
        testRental = new Rental();
        testRental.setUser(testUser);
        testRental.setFacility(testFacility);
        testRental.setStatus("CONFIRMED");
        testRental.setTotalPrice(50.0);
        testRental.setPaymentStatus("PENDING");
        testRental.setStartTime(LocalDateTime.now().plusDays(5));
        testRental.setEndTime(LocalDateTime.now().plusDays(5).plusHours(2));
        testRental = rentalRepository.save(testRental);

        Payment payment = new Payment(testRental, "pi_failed_payment", 50.0, "fail@example.com");
        payment = paymentRepository.save(payment);

        // Simulate failure
        payment.setStatus("FAILED");
        payment.setFailureMessage("Your card was declined");
        paymentRepository.save(payment);

        // Verify
        var found = paymentRepository.findByRentalId(testRental.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo("FAILED");
        assertThat(found.get().getFailureMessage()).isEqualTo("Your card was declined");
    }

    /**
     * Test: Creating rental sets totalPrice correctly for payment
     */
    @Test
    void whenRentalCreated_thenTotalPriceIsCalculated() {
        // Create rental via API
        RentalRequestDTO request = new RentalRequestDTO();
        request.setUserId(testUser.getId());
        request.setFacilityId(testFacility.getId());
        request.setStartTime(LocalDateTime.now().plusDays(6).withHour(10).withMinute(0).withSecond(0).withNano(0));
        request.setEndTime(LocalDateTime.now().plusDays(6).withHour(12).withMinute(0).withSecond(0).withNano(0));

        Integer rentalIdInt = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/rentals/rental")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("status", equalTo("CONFIRMED"))
                .extract()
                .path("id");

        // Verify rental has totalPrice set (2 hours * 20€/hour = 40€)
        Rental createdRental = rentalRepository.findById(rentalIdInt.longValue()).orElseThrow();
        assertThat(createdRental.getTotalPrice()).isEqualTo(40.0);
        assertThat(createdRental.getPaymentStatus()).isEqualTo("UNPAID");
    }

    /**
     * Test: existsByRentalId works correctly
     */
    @Test
    void whenCheckPaymentExists_thenReturnsCorrectResult() {
        // Setup rental without payment
        testRental = new Rental();
        testRental.setUser(testUser);
        testRental.setFacility(testFacility);
        testRental.setStatus("CONFIRMED");
        testRental.setTotalPrice(30.0);
        testRental.setPaymentStatus("UNPAID");
        testRental.setStartTime(LocalDateTime.now().plusDays(7));
        testRental.setEndTime(LocalDateTime.now().plusDays(7).plusHours(1));
        testRental = rentalRepository.save(testRental);

        // No payment exists yet
        assertThat(paymentRepository.existsByRentalId(testRental.getId())).isFalse();

        // Create payment
        Payment payment = new Payment(testRental, "pi_exists_test", 30.0, "exists@example.com");
        paymentRepository.save(payment);

        // Payment now exists
        assertThat(paymentRepository.existsByRentalId(testRental.getId())).isTrue();
    }
}
