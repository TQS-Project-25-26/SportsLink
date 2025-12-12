package tqs.sportslink.C_Tests_controller;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tqs.sportslink.boundary.PaymentController;
import tqs.sportslink.config.TestSecurityConfig;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Payment;
import tqs.sportslink.data.model.Rental;
import tqs.sportslink.data.model.User;
import tqs.sportslink.service.StripePaymentService;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class PaymentControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private StripePaymentService stripePaymentService;

        private Rental mockRental;
        private Payment mockPayment;

        @BeforeEach
        void setUp() {
                User mockUser = new User();
                mockUser.setId(1L);
                mockUser.setEmail("user@example.com");
                mockUser.setName("Test User");

                Facility mockFacility = new Facility();
                mockFacility.setId(1L);
                mockFacility.setName("Padel Arena");

                mockRental = new Rental();
                mockRental.setId(1L);
                mockRental.setUser(mockUser);
                mockRental.setFacility(mockFacility);
                mockRental.setTotalPrice(30.0);
                mockRental.setPaymentStatus("PENDING");
                mockRental.setStartTime(LocalDateTime.now().plusDays(1));
                mockRental.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

                mockPayment = new Payment(mockRental, "pi_test_123", 30.0, "user@example.com");
                mockPayment.setId(1L);
        }

        @Test
        void whenGetPaymentStatus_validRentalId_thenReturns200() throws Exception {
                mockPayment.setStatus("SUCCEEDED");
                mockPayment.setReceiptUrl("https://receipt.stripe.com/test");

                when(stripePaymentService.getPaymentByRentalId(1L)).thenReturn(Optional.of(mockPayment));

                mockMvc.perform(get("/api/payments/status/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.paymentId", is(1)))
                                .andExpect(jsonPath("$.rentalId", is(1)))
                                .andExpect(jsonPath("$.status", is("SUCCEEDED")))
                                .andExpect(jsonPath("$.amount", is(30.0)))
                                .andExpect(jsonPath("$.receiptUrl", is("https://receipt.stripe.com/test")));
        }

        @Test
        void whenGetPaymentStatus_invalidRentalId_thenReturns404() throws Exception {
                when(stripePaymentService.getPaymentByRentalId(999L)).thenReturn(Optional.empty());

                mockMvc.perform(get("/api/payments/status/999"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void whenGetStripeConfig_thenReturnsPublishableKey() throws Exception {
                mockMvc.perform(get("/api/payments/config"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.publishableKey", notNullValue()));
        }

        @Test
        void whenCreatePaymentIntent_rentalNotFound_thenReturns404() throws Exception {
                when(stripePaymentService.createPaymentIntent(anyLong(), anyString()))
                                .thenThrow(new NoSuchElementException("Rental not found: 999"));

                mockMvc.perform(post("/api/payments/create-intent/999")
                                .param("email", "test@example.com"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error", containsString("Rental not found")));
        }

        @Test
        void whenCreatePaymentIntent_invalidPrice_thenReturns400() throws Exception {
                when(stripePaymentService.createPaymentIntent(anyLong(), anyString()))
                                .thenThrow(new IllegalArgumentException("Invalid rental total price"));

                mockMvc.perform(post("/api/payments/create-intent/1")
                                .param("email", "test@example.com"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error", containsString("Invalid rental total price")));
        }

        @Test
        void whenCreatePaymentIntent_paymentAlreadyCompleted_thenReturns400() throws Exception {
                when(stripePaymentService.createPaymentIntent(anyLong(), anyString()))
                                .thenThrow(new IllegalStateException("Payment already completed for this rental"));

                mockMvc.perform(post("/api/payments/create-intent/1")
                                .param("email", "test@example.com"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error", containsString("Payment already completed")));
        }

        @Test
        void whenGetPaymentStatus_withPendingPayment_thenReturnsPending() throws Exception {
                mockPayment.setStatus("PENDING");

                when(stripePaymentService.getPaymentByRentalId(1L)).thenReturn(Optional.of(mockPayment));

                mockMvc.perform(get("/api/payments/status/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("PENDING")))
                                .andExpect(jsonPath("$.customerEmail", is("user@example.com")));
        }

        @Test
        void whenGetPaymentStatus_withFailedPayment_thenReturnsFailed() throws Exception {
                mockPayment.setStatus("FAILED");

                when(stripePaymentService.getPaymentByRentalId(1L)).thenReturn(Optional.of(mockPayment));

                mockMvc.perform(get("/api/payments/status/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("FAILED")));
        }

        @Test
        void whenCreatePaymentIntent_stripeException_thenReturns500() throws Exception {
                // StripeException is abstract; easiest is to mock it.
                com.stripe.exception.StripeException stripeEx = mock(com.stripe.exception.StripeException.class);
                when(stripeEx.getMessage()).thenReturn("boom");

                when(stripePaymentService.createPaymentIntent(anyLong(), anyString()))
                        .thenThrow(stripeEx);

                mockMvc.perform(post("/api/payments/create-intent/1")
                                .param("email", "test@example.com"))
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.error", containsString("Payment service error: boom")));
        }

        @Test
        void whenWebhookSuccess_thenReturns200() throws Exception {
                doNothing().when(stripePaymentService).handleWebhookEvent(anyString(), anyString());

                mockMvc.perform(post("/api/payments/webhook")
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .header("Stripe-Signature", "sig")
                                .content("{}"))
                        .andExpect(status().isOk())
                        .andExpect(content().string("Webhook processed"));
        }

        @Test
        void whenWebhookInvalidSignature_thenReturns400() throws Exception {
                doThrow(new com.stripe.exception.SignatureVerificationException("bad sig", "sig"))
                        .when(stripePaymentService).handleWebhookEvent(anyString(), anyString());

                mockMvc.perform(post("/api/payments/webhook")
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .header("Stripe-Signature", "sig")
                                .content("{}"))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().string("Invalid signature"));
        }

        @Test
        void whenWebhookGenericError_thenReturns500() throws Exception {
                doThrow(new RuntimeException("unexpected"))
                        .when(stripePaymentService).handleWebhookEvent(anyString(), anyString());

                mockMvc.perform(post("/api/payments/webhook")
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .header("Stripe-Signature", "sig")
                                .content("{}"))
                        .andExpect(status().isInternalServerError())
                        .andExpect(content().string("Webhook processing error"));
        }

}
