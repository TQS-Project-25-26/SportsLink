package tqs.sportslink.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilitySuggestionDTO {
    private Long facilityId;
    private String name;
    private String imageUrl;
    private String address;
    private String city;
    private Double pricePerHour;
    private Double rating;
    private String reason; // Why this facility is suggested
    private Double score; // Confidence score of the suggestion (0-100)
    private Double distanceKm; // Distance from user in kilometers (optional)
}
