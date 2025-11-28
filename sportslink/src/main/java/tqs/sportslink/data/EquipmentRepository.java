package tqs.sportslink.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tqs.sportslink.data.model.Equipment;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    
    // Query methods simples - Spring Data gera automaticamente
    List<Equipment> findByFacilityId(Long facilityId);
    
    List<Equipment> findByType(String type);
}
