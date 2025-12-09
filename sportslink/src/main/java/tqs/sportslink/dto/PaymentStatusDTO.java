package tqs.sportslink.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for payment status response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusDTO {
    private Long paymentId;
    private Long rentalId;
    private String status;
    private Double amount;
    private String currency;
    private String receiptUrl;
    private String customerEmail;
}
