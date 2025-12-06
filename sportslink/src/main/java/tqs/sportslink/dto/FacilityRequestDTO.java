package tqs.sportslink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tqs.sportslink.data.model.Sport;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacilityRequestDTO {

    @NotBlank
    private String name;

    @NotNull
    private List<Sport> sports;

    @NotBlank
    private String city;

    @NotBlank
    private String address;

    private String description;

    @NotNull
    private Double pricePerHour;

    @NotBlank
    private String openingTime;  // formato "08:00"

    @NotBlank
    private String closingTime; // formato "22:00"
}
