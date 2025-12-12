package tqs.sportslink.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import tqs.sportslink.data.model.Sport;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacilityResponseDTO {
    
    private Long id;
    private String name;
    private String imageUrl;
    private List<Sport> sports;
    private String city;
    private String address;
    private String description;
    private Double pricePerHour;
    private Double rating;
    private String openingTime;
    private String closingTime;
    
}
