package tqs.sportslink.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tqs.sportslink.data.model.Facility;

import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {
    
    // Query methods simples - Spring Data gera automaticamente
    List<Facility> findByCityAndSportType(String city, String sportType);
    
    List<Facility> findByCity(String city);
    
    List<Facility> findBySportType(String sportType);
}
