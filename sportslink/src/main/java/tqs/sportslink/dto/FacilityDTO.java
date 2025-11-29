package tqs.sportslink.dto;

public class FacilityDTO {
    private Long id;
    private String name;
    private String sportType;
    private String address;
    private String city;
    private String description;
    private Double pricePerHour;
    private Double rating;
    private String status;
    private String openingTime;
    private String closingTime;

    public FacilityDTO() {}

    public FacilityDTO(Long id, String name, String sportType, String address, String city, String description, Double pricePerHour, Double rating, String status, String openingTime, String closingTime) {
        this.id = id;
        this.name = name;
        this.sportType = sportType;
        this.address = address;
        this.city = city;
        this.description = description;
        this.pricePerHour = pricePerHour;
        this.rating = rating;
        this.status = status;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    // Getters e setters (gere na IDE)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSportType() { return sportType; }
    public void setSportType(String sportType) { this.sportType = sportType; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(Double pricePerHour) { this.pricePerHour = pricePerHour; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOpeningTime() { return openingTime; }
    public void setOpeningTime(String openingTime) { this.openingTime = openingTime; }
    public String getClosingTime() { return closingTime; }
    public void setClosingTime(String closingTime) { this.closingTime = closingTime; }
}
