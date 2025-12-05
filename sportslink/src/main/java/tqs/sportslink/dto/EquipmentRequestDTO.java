package tqs.sportslink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentRequestDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    private String description;

    @NotNull
    private Integer quantity;

    private Double pricePerHour;

    @NotBlank
    private String status; // AVAILABLE, MAINTENANCE, UNAVAILABLE
}
