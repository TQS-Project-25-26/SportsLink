package tqs.sportslink.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for PaymentIntent creation response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentDTO {
    private String clientSecret;
    private Long paymentId;
    private String publishableKey;
}
