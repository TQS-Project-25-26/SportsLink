package tqs.sportslink.dto;

public class FacilityResponseDTO {
    
    private Long id;
    private String name;
    private String sportType;
    private String city;
    private String address;
    private String description;
    private Double pricePerHour;
    private Double rating;
    private String openingTime;
    private String closingTime;
    
    // Constructors
    public FacilityResponseDTO() {
    }
    
    public FacilityResponseDTO(Long id, String name, String sportType, String city, String address, 
                              String description, Double pricePerHour, Double rating, 
                              String openingTime, String closingTime) {
        this.id = id;
        this.name = name;
        this.sportType = sportType;
        this.city = city;
        this.address = address;
        this.description = description;
        this.pricePerHour = pricePerHour;
        this.rating = rating;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
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
    
    public String getSportType() {
        return sportType;
    }
    
    public void setSportType(String sportType) {
        this.sportType = sportType;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Double getPricePerHour() {
        return pricePerHour;
    }
    
    public void setPricePerHour(Double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }
    
    public Double getRating() {
        return rating;
    }
    
    public void setRating(Double rating) {
        this.rating = rating;
    }
    
    public String getOpeningTime() {
        return openingTime;
    }
    
    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }
    
    public String getClosingTime() {
        return closingTime;
    }
    
    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }
}
