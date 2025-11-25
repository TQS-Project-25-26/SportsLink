package tqs.sportslink.dto;

import java.time.LocalDateTime;
import java.util.List;

public class RentalResponseDTO {
    private Long id;
    private Long facilityId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private List<String> equipments; // Nomes dos equipamentos

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFacilityId() { return facilityId; }
    public void setFacilityId(Long facilityId) { this.facilityId = facilityId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getEquipments() { return equipments; }
    public void setEquipments(List<String> equipments) { this.equipments = equipments; }
}