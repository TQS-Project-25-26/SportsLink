package tqs.sportslink.B_Tests_unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.sportslink.data.PaymentRepository;
import tqs.sportslink.data.RentalRepository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Payment;
import tqs.sportslink.data.model.Rental;
import tqs.sportslink.data.model.User;
import tqs.sportslink.service.StripePaymentService;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnitStripePaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RentalRepository rentalRepository;

    @InjectMocks
    private StripePaymentService stripePaymentService;

    private Rental mockRental;
    private User mockUser;
    private Facility mockFacility;
    private Payment mockPayment;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("user@example.com");
        mockUser.setName("Test User");

        mockFacility = new Facility();
        mockFacility.setId(1L);
        mockFacility.setName("Padel Arena");
        mockFacility.setPricePerHour(15.0);

        mockRental = new Rental();
        mockRental.setId(1L);
        mockRental.setUser(mockUser);
        mockRental.setFacility(mockFacility);
        mockRental.setTotalPrice(30.0);
        mockRental.setPaymentStatus("UNPAID");
        mockRental.setStartTime(LocalDateTime.now().plusDays(1));
        mockRental.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        mockPayment = new Payment(mockRental, "pi_test_123", 30.0, "user@example.com");
        mockPayment.setId(1L);
    }

    @Test
    void whenGetPaymentByRentalId_withExistingPayment_thenReturnPayment() {
        when(paymentRepository.findByRentalId(1L)).thenReturn(Optional.of(mockPayment));

        Optional<Payment> result = stripePaymentService.getPaymentByRentalId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getAmount()).isEqualTo(30.0);
        verify(paymentRepository).findByRentalId(1L);
    }

    @Test
    void whenGetPaymentByRentalId_withNoPayment_thenReturnEmpty() {
        when(paymentRepository.findByRentalId(999L)).thenReturn(Optional.empty());

        Optional<Payment> result = stripePaymentService.getPaymentByRentalId(999L);

        assertThat(result).isEmpty();
        verify(paymentRepository).findByRentalId(999L);
    }

    @Test
    void whenGetPaymentByRentalId_withReceiptUrl_thenNoStripeFetch() {
        mockPayment.setReceiptUrl("https://receipt.stripe.com/test");
        when(paymentRepository.findByRentalId(1L)).thenReturn(Optional.of(mockPayment));

        Optional<Payment> result = stripePaymentService.getPaymentByRentalId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getReceiptUrl()).isEqualTo("https://receipt.stripe.com/test");
        // Should not try to fetch from Stripe since receipt URL already exists
        verify(paymentRepository).findByRentalId(1L);
    }

    @Test
    void whenCreatePaymentIntent_withNullTotalPrice_shouldThrowException() {
        mockRental.setTotalPrice(null);
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));
        when(paymentRepository.findByRentalId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stripePaymentService.createPaymentIntent(1L, "test@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid rental total price");
    }

    @Test
    void whenCreatePaymentIntent_withZeroPrice_shouldThrowException() {
        mockRental.setTotalPrice(0.0);
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));
        when(paymentRepository.findByRentalId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stripePaymentService.createPaymentIntent(1L, "test@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid rental total price");
    }

    @Test
    void whenCreatePaymentIntent_withNegativePrice_shouldThrowException() {
        mockRental.setTotalPrice(-10.0);
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));
        when(paymentRepository.findByRentalId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stripePaymentService.createPaymentIntent(1L, "test@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid rental total price");
    }

    @Test
    void whenCreatePaymentIntent_rentalNotFound_shouldThrowException() {
        when(rentalRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stripePaymentService.createPaymentIntent(999L, "test@example.com"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Rental not found");
    }

    @Test
    void whenCreatePaymentIntent_paymentAlreadySucceeded_shouldThrowException() {
        mockPayment.setStatus("SUCCEEDED");
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));
        when(paymentRepository.findByRentalId(1L)).thenReturn(Optional.of(mockPayment));

        assertThatThrownBy(() -> stripePaymentService.createPaymentIntent(1L, "test@example.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Payment already completed");
    }

    @Test
    void whenPaymentCreated_thenDefaultStatusIsPending() {
        Payment newPayment = new Payment(mockRental, "pi_new", 25.0, "new@example.com");

        assertThat(newPayment.getStatus()).isEqualTo("PENDING");
        assertThat(newPayment.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void whenPaymentUpdated_thenStatusChanges() {
        mockPayment.setStatus("SUCCEEDED");
        mockPayment.setStripeChargeId("ch_test_123");
        mockPayment.setReceiptUrl("https://receipt.stripe.com/123");

        assertThat(mockPayment.getStatus()).isEqualTo("SUCCEEDED");
        assertThat(mockPayment.getStripeChargeId()).isEqualTo("ch_test_123");
        assertThat(mockPayment.getReceiptUrl()).isEqualTo("https://receipt.stripe.com/123");
    }

    @Test
    void whenPaymentFailed_thenFailureMessageIsSet() {
        mockPayment.setStatus("FAILED");
        mockPayment.setFailureMessage("Card declined");

        assertThat(mockPayment.getStatus()).isEqualTo("FAILED");
        assertThat(mockPayment.getFailureMessage()).isEqualTo("Card declined");
    }

    @Test
    void whenPaymentCreated_thenRentalRelationshipIsCorrect() {
        Payment payment = new Payment(mockRental, "pi_relation_test", 30.0, "relation@example.com");

        assertThat(payment.getRental()).isEqualTo(mockRental);
        assertThat(payment.getRental().getId()).isEqualTo(1L);
        assertThat(payment.getRental().getFacility().getName()).isEqualTo("Padel Arena");
    }

    @Test
    void whenPaymentCreated_thenCustomerEmailIsStored() {
        Payment payment = new Payment(mockRental, "pi_email_test", 30.0, "customer@example.com");

        assertThat(payment.getCustomerEmail()).isEqualTo("customer@example.com");
    }

    @Test
    void whenPaymentAmountCalculated_thenCentsConversionIsCorrect() {
        // Testing that 30.0 EUR should convert to 3000 cents
        double totalPrice = 30.0;
        long amountInCents = Math.round(totalPrice * 100);

        assertThat(amountInCents).isEqualTo(3000L);
    }

    @Test
    void whenPaymentAmountWithDecimals_thenCentsConversionIsCorrect() {
        // Testing that 19.99 EUR should convert to 1999 cents
        double totalPrice = 19.99;
        long amountInCents = Math.round(totalPrice * 100);

        assertThat(amountInCents).isEqualTo(1999L);
    }

    @Test
    void whenCreatePaymentIntent_Success_shouldReturnClientSecret() {
        // Prepare data
        mockRental.setTotalPrice(50.0);
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(mockRental));
        when(paymentRepository.findByRentalId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(100L);
            return p;
        });

        // Mock static PaymentIntent.create
        try (org.mockito.MockedStatic<com.stripe.model.PaymentIntent> mockedPaymentIntent = mockStatic(com.stripe.model.PaymentIntent.class)) {
            com.stripe.model.PaymentIntent mockIntent = mock(com.stripe.model.PaymentIntent.class);
            when(mockIntent.getId()).thenReturn("pi_success_123");
            when(mockIntent.getClientSecret()).thenReturn("secret_123");
            
            mockedPaymentIntent.when(() -> com.stripe.model.PaymentIntent.create(any(com.stripe.param.PaymentIntentCreateParams.class)))
                    .thenReturn(mockIntent);

            // Execute
            StripePaymentService.PaymentIntentResult result = stripePaymentService.createPaymentIntent(1L, "user@example.com");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.clientSecret()).isEqualTo("secret_123");
            assertThat(result.paymentId()).isEqualTo(100L);
            
            // Verify repository calls
            verify(paymentRepository).save(any(Payment.class));
            verify(rentalRepository).save(mockRental);
            assertThat(mockRental.getPaymentStatus()).isEqualTo("PENDING");
        } catch (com.stripe.exception.StripeException e) {
            org.junit.jupiter.api.Assertions.fail("StripeException should not be thrown");
        }
    }

    @Test
    void whenHandleWebhookEvent_PaymentSucceeded_shouldUpdateStatus() {
        String payload = "{}";
        String sigHeader = "sig";

        try (org.mockito.MockedStatic<com.stripe.net.Webhook> mockedWebhook = mockStatic(com.stripe.net.Webhook.class)) {
            // Mock Event
            com.stripe.model.Event mockEvent = mock(com.stripe.model.Event.class);
            when(mockEvent.getType()).thenReturn("payment_intent.succeeded");

            // Mock DataObjectDeserializer and PaymentIntent inside Event
            com.stripe.model.EventDataObjectDeserializer mockDeserializer = mock(com.stripe.model.EventDataObjectDeserializer.class);
            com.stripe.model.PaymentIntent mockIntent = mock(com.stripe.model.PaymentIntent.class);
            when(mockIntent.getId()).thenReturn("pi_webhook_123");
            
            when(mockEvent.getDataObjectDeserializer()).thenReturn(mockDeserializer);
            when(mockDeserializer.getObject()).thenReturn(Optional.of(mockIntent));

            mockedWebhook.when(() -> com.stripe.net.Webhook.constructEvent(eq(payload), eq(sigHeader), any()))
                    .thenReturn(mockEvent);

            // Mock Repository finding payment
            Payment existingPayment = new Payment(mockRental, "pi_webhook_123", 50.0, "user@example.com");
            when(paymentRepository.findByStripePaymentIntentId("pi_webhook_123")).thenReturn(Optional.of(existingPayment));

            // Execute
            try {
                stripePaymentService.handleWebhookEvent(payload, sigHeader);
            } catch (Exception e) {
                 org.junit.jupiter.api.Assertions.fail("Exception should not be thrown");
            }

            // Assert
            assertThat(existingPayment.getStatus()).isEqualTo("SUCCEEDED");
            assertThat(existingPayment.getRental().getPaymentStatus()).isEqualTo("PAID");
            verify(paymentRepository).save(existingPayment);
            verify(rentalRepository).save(mockRental);
        }
    }

    @Test
    void whenHandleWebhookEvent_PaymentFailed_withStripeErrorMessage_shouldMarkPaymentFailed() {
        String payload = "{}";
        String sigHeader = "sig";

        try (org.mockito.MockedStatic<com.stripe.net.Webhook> mockedWebhook = mockStatic(com.stripe.net.Webhook.class)) {
            com.stripe.model.Event mockEvent = mock(com.stripe.model.Event.class);
            when(mockEvent.getType()).thenReturn("payment_intent.payment_failed");

            com.stripe.model.EventDataObjectDeserializer mockDeserializer =
                    mock(com.stripe.model.EventDataObjectDeserializer.class);

            com.stripe.model.PaymentIntent mockIntent = mock(com.stripe.model.PaymentIntent.class);
            when(mockIntent.getId()).thenReturn("pi_failed_123");

            // ✅ StripeError (not PaymentIntent.LastPaymentError)
            com.stripe.model.StripeError mockError = mock(com.stripe.model.StripeError.class);
            when(mockError.getMessage()).thenReturn("Card declined");
            when(mockIntent.getLastPaymentError()).thenReturn(mockError);

            when(mockEvent.getDataObjectDeserializer()).thenReturn(mockDeserializer);
            when(mockDeserializer.getObject()).thenReturn(Optional.of(mockIntent));

            mockedWebhook.when(() -> com.stripe.net.Webhook.constructEvent(eq(payload), eq(sigHeader), any()))
                    .thenReturn(mockEvent);

            Payment existingPayment = new Payment(mockRental, "pi_failed_123", 30.0, "user@example.com");
            when(paymentRepository.findByStripePaymentIntentId("pi_failed_123")).thenReturn(Optional.of(existingPayment));

            stripePaymentService.handleWebhookEvent(payload, sigHeader);

            assertThat(existingPayment.getStatus()).isEqualTo("FAILED");
            assertThat(existingPayment.getFailureMessage()).isEqualTo("Card declined");
            verify(paymentRepository).save(existingPayment);
            verify(rentalRepository, never()).save(any());
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("Exception should not be thrown");
        }
    }


    @Test
    void whenHandleWebhookEvent_PaymentFailed_withoutStripeError_shouldUseDefaultMessage() {
        String payload = "{}";
        String sigHeader = "sig";

        try (org.mockito.MockedStatic<com.stripe.net.Webhook> mockedWebhook = mockStatic(com.stripe.net.Webhook.class)) {
            com.stripe.model.Event mockEvent = mock(com.stripe.model.Event.class);
            when(mockEvent.getType()).thenReturn("payment_intent.payment_failed");

            com.stripe.model.EventDataObjectDeserializer mockDeserializer =
                    mock(com.stripe.model.EventDataObjectDeserializer.class);

            com.stripe.model.PaymentIntent mockIntent = mock(com.stripe.model.PaymentIntent.class);
            when(mockIntent.getId()).thenReturn("pi_failed_no_error");
            when(mockIntent.getLastPaymentError()).thenReturn(null);

            when(mockEvent.getDataObjectDeserializer()).thenReturn(mockDeserializer);
            when(mockDeserializer.getObject()).thenReturn(Optional.of(mockIntent));

            mockedWebhook.when(() -> com.stripe.net.Webhook.constructEvent(eq(payload), eq(sigHeader), any()))
                    .thenReturn(mockEvent);

            Payment existingPayment = new Payment(mockRental, "pi_failed_no_error", 30.0, "user@example.com");
            when(paymentRepository.findByStripePaymentIntentId("pi_failed_no_error"))
                    .thenReturn(Optional.of(existingPayment));

            stripePaymentService.handleWebhookEvent(payload, sigHeader);

            assertThat(existingPayment.getStatus()).isEqualTo("FAILED");
            assertThat(existingPayment.getFailureMessage()).isEqualTo("Payment failed");
            verify(paymentRepository).save(existingPayment);
            verify(rentalRepository, never()).save(any());
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("Exception should not be thrown");
        }
    }

    @Test
    void whenHandleWebhookEvent_ChargeSucceeded_shouldStoreChargeIdAndReceiptUrl() {
        String payload = "{}";
        String sigHeader = "sig";

        try (org.mockito.MockedStatic<com.stripe.net.Webhook> mockedWebhook = mockStatic(com.stripe.net.Webhook.class)) {
            com.stripe.model.Event mockEvent = mock(com.stripe.model.Event.class);
            when(mockEvent.getType()).thenReturn("charge.succeeded");

            com.stripe.model.EventDataObjectDeserializer mockDeserializer =
                    mock(com.stripe.model.EventDataObjectDeserializer.class);

            com.stripe.model.Charge mockCharge = mock(com.stripe.model.Charge.class);
            when(mockCharge.getPaymentIntent()).thenReturn("pi_charge_123");
            when(mockCharge.getId()).thenReturn("ch_123");
            when(mockCharge.getReceiptUrl()).thenReturn("https://receipt.stripe.com/ch_123");

            when(mockEvent.getDataObjectDeserializer()).thenReturn(mockDeserializer);
            when(mockDeserializer.getObject()).thenReturn(Optional.of(mockCharge));

            mockedWebhook.when(() -> com.stripe.net.Webhook.constructEvent(eq(payload), eq(sigHeader), any()))
                    .thenReturn(mockEvent);

            Payment existingPayment = new Payment(mockRental, "pi_charge_123", 30.0, "user@example.com");
            existingPayment.setId(55L);
            when(paymentRepository.findByStripePaymentIntentId("pi_charge_123")).thenReturn(Optional.of(existingPayment));

            stripePaymentService.handleWebhookEvent(payload, sigHeader);

            assertThat(existingPayment.getStripeChargeId()).isEqualTo("ch_123");
            assertThat(existingPayment.getReceiptUrl()).isEqualTo("https://receipt.stripe.com/ch_123");
            verify(paymentRepository).save(existingPayment);
            verify(rentalRepository, never()).save(any()); // charge handler doesn't update rental
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("Exception should not be thrown");
        }
    }

    @Test
    void whenHandleWebhookEvent_ChargeSucceeded_withoutPaymentIntentId_shouldDoNothing() {
        String payload = "{}";
        String sigHeader = "sig";

        try (org.mockito.MockedStatic<com.stripe.net.Webhook> mockedWebhook = mockStatic(com.stripe.net.Webhook.class)) {
            com.stripe.model.Event mockEvent = mock(com.stripe.model.Event.class);
            when(mockEvent.getType()).thenReturn("charge.succeeded");

            com.stripe.model.EventDataObjectDeserializer mockDeserializer =
                    mock(com.stripe.model.EventDataObjectDeserializer.class);

            com.stripe.model.Charge mockCharge = mock(com.stripe.model.Charge.class);
            when(mockCharge.getPaymentIntent()).thenReturn(null); // triggers early return

            when(mockEvent.getDataObjectDeserializer()).thenReturn(mockDeserializer);
            when(mockDeserializer.getObject()).thenReturn(Optional.of(mockCharge));

            mockedWebhook.when(() -> com.stripe.net.Webhook.constructEvent(eq(payload), eq(sigHeader), any()))
                    .thenReturn(mockEvent);

            stripePaymentService.handleWebhookEvent(payload, sigHeader);

            verify(paymentRepository, never()).findByStripePaymentIntentId(anyString());
            verify(paymentRepository, never()).save(any());
            verify(rentalRepository, never()).save(any());
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("Exception should not be thrown");
        }
    }

}
