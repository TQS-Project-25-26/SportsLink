package tqs.sportslink.A_Tests_repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tqs.sportslink.data.PaymentRepository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Payment;
import tqs.sportslink.data.model.Rental;
import tqs.sportslink.data.model.User;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    private User createUser() {
        User u = new User();
        u.setEmail("renter@test.com");
        u.setName("Renter");
        u.setPassword("pass");
        return entityManager.persistAndFlush(u);
    }

    private Facility createFacility() {
        Facility f = new Facility();
        f.setName("Arena");
        f.setCity("Aveiro");
        f.setAddress("Rua D");
        f.setStatus("ACTIVE");
        f.setPricePerHour(15.0);
        f.setLatitude(40.6);
        f.setLongitude(-8.6);
        f.setOpeningTime(LocalTime.of(8, 0));
        f.setClosingTime(LocalTime.of(22, 0));
        return entityManager.persistAndFlush(f);
    }

    private Rental createRental(User user, Facility facility) {
        Rental r = new Rental();
        r.setUser(user);
        r.setFacility(facility);
        r.setStatus("CONFIRMED");
        r.setTotalPrice(30.0);
        r.setPaymentStatus("PENDING");
        r.setStartTime(LocalDateTime.of(2025, 10, 10, 10, 0));
        r.setEndTime(LocalDateTime.of(2025, 10, 10, 12, 0));
        return entityManager.persistAndFlush(r);
    }

    @Test
    void whenFindByStripePaymentIntentId_thenReturnPayment() {
        // Setup
        User user = createUser();
        Facility facility = createFacility();
        Rental rental = createRental(user, facility);

        Payment payment = new Payment(rental, "pi_test_123456", 30.0, "test@example.com");
        entityManager.persistAndFlush(payment);

        // Test
        Optional<Payment> found = paymentRepository.findByStripePaymentIntentId("pi_test_123456");

        assertThat(found).isPresent();
        assertThat(found.get().getStripePaymentIntentId()).isEqualTo("pi_test_123456");
        assertThat(found.get().getAmount()).isEqualTo(30.0);
    }

    @Test
    void whenFindByStripePaymentIntentId_notExists_thenReturnEmpty() {
        Optional<Payment> found = paymentRepository.findByStripePaymentIntentId("pi_nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void whenFindByRentalId_thenReturnPayment() {
        // Setup
        User user = createUser();
        Facility facility = createFacility();
        Rental rental = createRental(user, facility);

        Payment payment = new Payment(rental, "pi_test_789", 30.0, "user@example.com");
        entityManager.persistAndFlush(payment);

        // Test
        Optional<Payment> found = paymentRepository.findByRentalId(rental.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getRental().getId()).isEqualTo(rental.getId());
        assertThat(found.get().getCustomerEmail()).isEqualTo("user@example.com");
    }

    @Test
    void whenFindByRentalId_notExists_thenReturnEmpty() {
        Optional<Payment> found = paymentRepository.findByRentalId(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void whenExistsByRentalId_withPayment_thenReturnTrue() {
        // Setup
        User user = createUser();
        Facility facility = createFacility();
        Rental rental = createRental(user, facility);

        Payment payment = new Payment(rental, "pi_test_abc", 30.0, "check@example.com");
        entityManager.persistAndFlush(payment);

        // Test
        boolean exists = paymentRepository.existsByRentalId(rental.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void whenExistsByRentalId_withoutPayment_thenReturnFalse() {
        boolean exists = paymentRepository.existsByRentalId(999L);

        assertThat(exists).isFalse();
    }

    @Test
    void whenSavePayment_thenPaymentIsPersisted() {
        // Setup
        User user = createUser();
        Facility facility = createFacility();
        Rental rental = createRental(user, facility);

        Payment payment = new Payment(rental, "pi_new_payment", 45.0, "new@example.com");

        // Test
        Payment saved = paymentRepository.save(payment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo("PENDING");
        assertThat(saved.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void whenUpdatePaymentStatus_thenStatusIsUpdated() {
        // Setup
        User user = createUser();
        Facility facility = createFacility();
        Rental rental = createRental(user, facility);

        Payment payment = new Payment(rental, "pi_update_test", 30.0, "update@example.com");
        entityManager.persistAndFlush(payment);

        // Update status
        payment.setStatus("SUCCEEDED");
        payment.setReceiptUrl("https://receipt.stripe.com/test");
        paymentRepository.save(payment);

        // Verify
        Optional<Payment> found = paymentRepository.findByStripePaymentIntentId("pi_update_test");
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo("SUCCEEDED");
        assertThat(found.get().getReceiptUrl()).isEqualTo("https://receipt.stripe.com/test");
    }
}
