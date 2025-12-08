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
    
    // Constructor for service layer usage
    public FacilityResponseDTO(Long id, String name, String imageUrl, List<Sport> sports, String city, String address, 
                              String description, Double pricePerHour, Double rating) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.sports = sports;
        this.city = city;
        this.address = address;
        this.description = description;
        this.pricePerHour = pricePerHour;
        this.rating = rating;
    }
    
}
