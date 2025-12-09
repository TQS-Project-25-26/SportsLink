package tqs.sportslink.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tqs.sportslink.data.model.Payment;

import java.util.Optional;

/**
 * Repository for Payment entity operations.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by Stripe PaymentIntent ID
     */
    Optional<Payment> findByStripePaymentIntentId(String paymentIntentId);

    /**
     * Find payment by rental ID
     */
    Optional<Payment> findByRentalId(Long rentalId);

    /**
     * Check if payment exists for a rental
     */
    boolean existsByRentalId(Long rentalId);
}
