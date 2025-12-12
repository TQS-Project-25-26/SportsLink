package tqs.sportslink.service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tqs.sportslink.data.PaymentRepository;
import tqs.sportslink.data.RentalRepository;
import tqs.sportslink.data.model.Payment;
import tqs.sportslink.data.model.Rental;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Service for handling Stripe payment operations.
 */
@Service
public class StripePaymentService {

    private static final Logger logger = LoggerFactory.getLogger(StripePaymentService.class);

    private static final String SUCCEEDED = "SUCCEEDED";

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;

    public StripePaymentService(PaymentRepository paymentRepository, RentalRepository rentalRepository) {
        this.paymentRepository = paymentRepository;
        this.rentalRepository = rentalRepository;
    }

    @PostConstruct
    public void init() {
        configureStripeApiKey(stripeApiKey);
        logger.info("Stripe API initialized");
    }

    private static void configureStripeApiKey(String apiKey) {
        Stripe.apiKey = apiKey;
    }

    /**
     * Create a PaymentIntent for a rental booking.
     * This creates a payment record and returns the client secret for frontend use.
     */
    @Transactional
    public PaymentIntentResult createPaymentIntent(Long rentalId, String customerEmail) throws StripeException {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NoSuchElementException("Rental not found: " + rentalId));

        // Check if payment already exists
        Optional<Payment> existingPayment = paymentRepository.findByRentalId(rentalId);
        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            if (SUCCEEDED.equals(payment.getStatus())) {
                throw new IllegalStateException("Payment already completed for this rental");
            }
            // Return existing payment intent
            try {
                PaymentIntent intent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());
                return new PaymentIntentResult(intent.getClientSecret(), payment.getId());
            } catch (StripeException e) {
                // Intent might be invalid, create a new one
                logger.warn("Could not retrieve existing PaymentIntent, creating new one", e);
            }
        }

        // Calculate amount in cents
        Double totalPrice = rental.getTotalPrice();
        if (totalPrice == null || totalPrice <= 0) {
            throw new IllegalArgumentException("Invalid rental total price");
        }
        long amountInCents = Math.round(totalPrice * 100);

        // Create PaymentIntent with Stripe
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("eur")
                .setReceiptEmail(customerEmail) // Enables automatic email receipts
                .putMetadata("rentalId", rental.getId().toString())
                .putMetadata("facilityName", rental.getFacility().getName())
                .setDescription("SportsLink Rental #" + rental.getId() + " - " + rental.getFacility().getName())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        // Create payment record
        Payment payment = new Payment(rental, paymentIntent.getId(), totalPrice, customerEmail);
        paymentRepository.save(payment);

        // Update rental payment status
        rental.setPaymentStatus("PENDING");
        rentalRepository.save(rental);

        logger.info("Created PaymentIntent {} for rental {}", paymentIntent.getId(), rentalId);

        return new PaymentIntentResult(paymentIntent.getClientSecret(), payment.getId());
    }

    /**
     * Handle Stripe webhook events.
     */
    @Transactional
    public void handleWebhookEvent(String payload, String signatureHeader) throws SignatureVerificationException {
        Event event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);

        logger.info("Received Stripe webhook event: {}", event.getType());

        switch (event.getType()) {
            case "payment_intent.succeeded":
                handlePaymentSuccess(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentFailure(event);
                break;
            case "charge.succeeded":
                handleChargeSuccess(event);
                break;
            default:
                logger.debug("Unhandled event type: {}", event.getType());
        }
    }

    private void handlePaymentSuccess(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalStateException("Invalid PaymentIntent in event"));

        String paymentIntentId = paymentIntent.getId();

        paymentRepository.findByStripePaymentIntentId(paymentIntentId).ifPresent(payment -> {
            payment.setStatus(SUCCEEDED);
            paymentRepository.save(payment);

            Rental rental = payment.getRental();
            rental.setPaymentStatus("PAID");
            rentalRepository.save(rental);

            logger.info("Payment succeeded for rental {}", rental.getId());
        });
    }

    private void handlePaymentFailure(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalStateException("Invalid PaymentIntent in event"));

        String paymentIntentId = paymentIntent.getId();

        paymentRepository.findByStripePaymentIntentId(paymentIntentId).ifPresent(payment -> {
            payment.setStatus("FAILED");
            payment.setFailureMessage(paymentIntent.getLastPaymentError() != null
                    ? paymentIntent.getLastPaymentError().getMessage()
                    : "Payment failed");
            paymentRepository.save(payment);

            logger.warn("Payment failed for rental {}", payment.getRental().getId());
        });
    }

    private void handleChargeSuccess(Event event) {
        Charge charge = (Charge) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalStateException("Invalid Charge in event"));

        String paymentIntentId = charge.getPaymentIntent();
        if (paymentIntentId == null)
            return;

        paymentRepository.findByStripePaymentIntentId(paymentIntentId).ifPresent(payment -> {
            payment.setStripeChargeId(charge.getId());
            payment.setReceiptUrl(charge.getReceiptUrl());
            paymentRepository.save(payment);

            logger.info("Updated payment {} with charge {} and receipt URL", payment.getId(), charge.getId());
        });
    }

    /**
     * Get payment status for a rental, fetching receipt URL from Stripe if needed.
     */
    @Transactional
    public Optional<Payment> getPaymentByRentalId(Long rentalId) {
        Optional<Payment> paymentOpt = paymentRepository.findByRentalId(rentalId);

        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();

            // If we don't have a receipt URL yet, try to fetch it from Stripe
            if (payment.getReceiptUrl() == null && payment.getStripePaymentIntentId() != null) {
                try {
                    PaymentIntent intent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());

                    // Check if payment succeeded and get the latest charge
                    if ("succeeded".equals(intent.getStatus()) && intent.getLatestCharge() != null) {
                        Charge charge = Charge.retrieve(intent.getLatestCharge());

                        if (charge.getReceiptUrl() != null) {
                            payment.setReceiptUrl(charge.getReceiptUrl());
                            payment.setStripeChargeId(charge.getId());
                            payment.setStatus(SUCCEEDED);
                            paymentRepository.save(payment);

                            // Also update rental payment status
                            Rental rental = payment.getRental();
                            rental.setPaymentStatus("PAID");
                            rentalRepository.save(rental);

                            logger.info("Fetched receipt URL for payment {}", payment.getId());
                        }
                    }
                } catch (StripeException e) {
                    logger.warn("Could not fetch receipt from Stripe: {}", e.getMessage());
                }
            }
        }

        return paymentOpt;
    }

    /**
     * Record for returning PaymentIntent creation result.
     */
    public record PaymentIntentResult(String clientSecret, Long paymentId) {
    }
}
