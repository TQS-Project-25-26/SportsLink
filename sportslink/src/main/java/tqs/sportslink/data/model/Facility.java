package tqs.sportslink.data.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facilities")
public class Facility {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @ElementCollection(targetClass = Sport.class)
    @CollectionTable(name = "facility_sports", joinColumns = @JoinColumn(name = "facility_id"))
    @Column(name = "sport")
    @Enumerated(EnumType.STRING)
    private List<Sport> sports = new ArrayList<>();
    
    @Column(nullable = false, length = 500)
    private String address;
    
    @Column(nullable = false, length = 100)
    private String city;
    
    @Column(length = 1000)
    private String description;
    
    @Column(precision = 10)
    private Double pricePerHour;

    @Column(precision = 3)
    private Double rating;    @Column(nullable = false, length = 50)
    private String status; // ACTIVE, MAINTENANCE, CLOSED
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = true)
    private User owner;
    
    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL)
    private List<Equipment> equipments = new ArrayList<>();
    
    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL)
    private List<Rental> rentals = new ArrayList<>();
    
    @Column
    private LocalTime openingTime; // "08:00"
    
    @Column
    private LocalTime closingTime; // "22:00"
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "ACTIVE";
        }
        if (rating == null) {
            rating = 0.0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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
    
    public List<Sport> getSports() {
        return sports;
    }
    
    public void setSportsType(List<Sport> sports) {
        this.sports = sports;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public User getOwner() {
        return owner;
    }
    
    public void setOwner(User owner) {
        this.owner = owner;
    }
    
    public List<Equipment> getEquipments() {
        return equipments;
    }
    
    public void setEquipments(List<Equipment> equipments) {
        this.equipments = equipments;
    }
    
    public List<Rental> getRentals() {
        return rentals;
    }
    
    public void setRentals(List<Rental> rentals) {
        this.rentals = rentals;
    }
    
    public LocalTime getOpeningTime() {
        return openingTime;
    }
    
    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }
    
    public LocalTime getClosingTime() {
        return closingTime;
    }
    
    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}