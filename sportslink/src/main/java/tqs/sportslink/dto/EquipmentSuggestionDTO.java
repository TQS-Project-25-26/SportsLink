package tqs.sportslink.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentSuggestionDTO {
    private Long equipmentId;
    private String name;
    private String type;
    private Double pricePerHour;
    private Integer quantity;
    private String reason; // Why this equipment is suggested
    private Double score; // Confidence score of the suggestion (0-100)
}
