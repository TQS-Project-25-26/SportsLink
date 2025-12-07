package tqs.sportslink.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerSuggestionDTO {
    private String type; // EQUIPMENT_ADDITION, MAINTENANCE_NEEDED, PRICE_ADJUSTMENT, etc.
    private Long facilityId;
    private String facilityName;
    private String title;
    private String description;
    private String priority; // HIGH, MEDIUM, LOW
    private Double potentialRevenue; // Optional - estimated revenue impact
}
