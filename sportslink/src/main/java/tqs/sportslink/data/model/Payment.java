package tqs.sportslink.data.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Payment entity to track Stripe payment records for rentals.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "payment" })
    private Rental rental;

    @Column(nullable = false, unique = true)
    private String stripePaymentIntentId;

    @Column
    private String stripeChargeId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, SUCCEEDED, FAILED, REFUNDED, CANCELLED

    @Column(length = 500)
    private String receiptUrl;

    @Column
    private String customerEmail;

    @Column(length = 500)
    private String failureMessage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
        if (currency == null) {
            currency = "EUR";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructor for creating new payment
    public Payment(Rental rental, String stripePaymentIntentId, Double amount, String customerEmail) {
        this.rental = rental;
        this.stripePaymentIntentId = stripePaymentIntentId;
        this.amount = amount;
        this.customerEmail = customerEmail;
        this.status = "PENDING";
        this.currency = "EUR";
    }
}
