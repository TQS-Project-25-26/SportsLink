package tqs.sportslink.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tqs.sportslink.data.model.Rental;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    
    // Buscar rentals por facility e período (service fará validação de conflito)
    // Buscar rentals por facility e período (service fará validação de conflito)
    List<Rental> findByFacilityIdAndStartTimeLessThanAndEndTimeGreaterThan(
        Long facilityId, LocalDateTime endTime, LocalDateTime startTime
    );
    
    // Buscar rentals por user
    List<Rental> findByUserId(Long userId);
    
    // Buscar rentals por facility
    List<Rental> findByFacilityId(Long facilityId);
}
