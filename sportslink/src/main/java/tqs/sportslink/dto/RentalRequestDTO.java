package tqs.sportslink.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class RentalRequestDTO {
    @NotNull
    private Long facilityId;
    @NotNull
    private LocalDateTime startTime;
    @NotNull
    private LocalDateTime endTime;
    private List<Long> equipmentIds; // Opcional

    // Getters and setters
    public Long getFacilityId() { return facilityId; }
    public void setFacilityId(Long facilityId) { this.facilityId = facilityId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public List<Long> getEquipmentIds() { return equipmentIds; }
    public void setEquipmentIds(List<Long> equipmentIds) { this.equipmentIds = equipmentIds; }
}