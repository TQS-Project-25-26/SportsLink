package tqs.sportslink.boundary;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.sportslink.dto.PaymentIntentDTO;
import tqs.sportslink.dto.PaymentStatusDTO;
import tqs.sportslink.service.StripePaymentService;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Controller for payment operations with Stripe.
 */
@RestController
@RequestMapping("")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private static final String ERROR_KEY = "error";

    private final StripePaymentService stripePaymentService;

    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    public PaymentController(StripePaymentService stripePaymentService) {
        this.stripePaymentService = stripePaymentService;
    }

    /**
     * Create a PaymentIntent for a rental.
     * Returns the client secret needed by the frontend to complete payment.
     */
    @PostMapping("/api/payments/create-intent/{rentalId}")
    public ResponseEntity<Object> createPaymentIntent(
            @PathVariable Long rentalId,
            @RequestParam String email) {
        try {
            StripePaymentService.PaymentIntentResult result = stripePaymentService.createPaymentIntent(rentalId, email);

            PaymentIntentDTO response = new PaymentIntentDTO(
                    result.clientSecret(),
                    result.paymentId(),
                    stripePublishableKey);

            return ResponseEntity.ok().body(response);

        } catch (NoSuchElementException e) {
            logger.error("Rental not found: {}", rentalId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(ERROR_KEY, e.getMessage()));

        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.error("Payment error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of(ERROR_KEY, e.getMessage()));

        } catch (StripeException e) {
            logger.error("Stripe error creating PaymentIntent", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(ERROR_KEY, "Payment service error: " + e.getMessage()));
        }
    }

    /**
     * Stripe webhook endpoint.
     * Handles payment confirmations and failures from Stripe.
     */
    @PostMapping("/api/payments/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            stripePaymentService.handleWebhookEvent(payload, sigHeader);
            return ResponseEntity.ok("Webhook processed");

        } catch (SignatureVerificationException e) {
            logger.error("Invalid Stripe webhook signature", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid signature");

        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Webhook processing error");
        }
    }

    /**
     * Get payment status for a rental.
     */
    @GetMapping("/api/payments/status/{rentalId}")
    public ResponseEntity<Object> getPaymentStatus(@PathVariable Long rentalId) {
        return stripePaymentService.getPaymentByRentalId(rentalId)
                .<ResponseEntity<Object>>map(payment -> {
                    PaymentStatusDTO dto = new PaymentStatusDTO(
                            payment.getId(),
                            payment.getRental().getId(),
                            payment.getStatus(),
                            payment.getAmount(),
                            payment.getCurrency(),
                            payment.getReceiptUrl(),
                            payment.getCustomerEmail());
                    return ResponseEntity.ok().body(dto);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Get the Stripe publishable key for frontend initialization.
     */
    @GetMapping("/api/payments/config")
    public ResponseEntity<Map<String, String>> getStripeConfig() {
        return ResponseEntity.ok(Map.of("publishableKey", stripePublishableKey));
    }
}
