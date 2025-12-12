package tqs.sportslink.B_Tests_unit;

import org.junit.jupiter.api.Test;
import tqs.sportslink.data.model.Payment;
import tqs.sportslink.data.model.Rental;
import tqs.sportslink.data.model.User;
import tqs.sportslink.data.model.Facility;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UnitPaymentTest {

    @Test
    void whenPaymentCreated_withNullStatusAndCurrency_thenDefaultsAreApplied() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");

        Facility facility = new Facility();
        facility.setName("Test Facility");

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setFacility(facility);
        rental.setTotalPrice(20.0);
        rental.setStartTime(LocalDateTime.now().plusDays(1));
        rental.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));

        // Act
        Payment payment = new Payment(rental, "pi_test_default", 20.0, "test@example.com");

        // Assert — THESE lines hit the uncovered code
        assertThat(payment.getStatus()).isEqualTo("PENDING");
        assertThat(payment.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void whenPaymentPersisted_thenDefaultsRemain() {
        Payment payment = new Payment(null, "pi_test", 10.0, "x@y.com");

        assertThat(payment.getStatus()).isEqualTo("PENDING");
        assertThat(payment.getCurrency()).isEqualTo("EUR");
    }
}
