package tqs.sportslink.data.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facilities")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
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
    private Double rating;
    
    @Column(nullable = false, length = 50)
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

}