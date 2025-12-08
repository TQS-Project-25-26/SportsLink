package tqs.sportslink.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSuggestionsDTO {
    private List<FacilitySuggestionDTO> facilitySuggestions;
    private List<EquipmentSuggestionDTO> equipmentSuggestions;
}
