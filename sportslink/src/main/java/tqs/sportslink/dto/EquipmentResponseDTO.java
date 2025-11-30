package tqs.sportslink.dto;

public class EquipmentResponseDTO {
    
    private Long id;
    private String name;
    private String type;
    private String description;
    private Integer quantity;
    private Double pricePerHour;
    private String status;
    
    // Constructors
    public EquipmentResponseDTO() {
    }
    
    public EquipmentResponseDTO(Long id, String name, String type, String description, 
                               Integer quantity, Double pricePerHour, String status) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.quantity = quantity;
        this.pricePerHour = pricePerHour;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Double getPricePerHour() {
        return pricePerHour;
    }
    
    public void setPricePerHour(Double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
