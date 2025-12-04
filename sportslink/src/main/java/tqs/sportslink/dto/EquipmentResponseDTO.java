package tqs.sportslink.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentResponseDTO {
    
    private Long id;
    private String name;
    private String type;
    private String description;
    private Integer quantity;
    private Double pricePerHour;
    private String status;
    
}
