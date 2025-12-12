package tqs.sportslink.data.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipments")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 100)
    private String type; // Racket, Ball, Net, Vest, etc.

    @Column(length = 500)
    private String description;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @ElementCollection(targetClass = Sport.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "equipment_sports", joinColumns = @JoinColumn(name = "equipment_id"))
    @Column(name = "sport")
    @Enumerated(EnumType.STRING)
    private List<Sport> sports = new ArrayList<>();

    @Column(nullable = false)
    private Integer quantity; // Total units available

    @Column(precision = 10)
    private Double pricePerHour;

    @Column(nullable = false, length = 50)
    private String status; // AVAILABLE, MAINTENANCE, UNAVAILABLE

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToMany(mappedBy = "equipments")
    private List<Rental> rentals = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "AVAILABLE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}