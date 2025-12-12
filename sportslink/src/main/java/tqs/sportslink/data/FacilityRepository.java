package tqs.sportslink.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Sport;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {

    // Query methods customizados para lidar com ElementCollection
    @Query("SELECT DISTINCT f FROM Facility f JOIN f.sports s WHERE f.city = :city AND s = :sportType")
    List<Facility> findByCityAndSportType(@Param("city") String city, @Param("sportType") Sport sportType);

    List<Facility> findByCity(String city);

    @Query("SELECT DISTINCT f FROM Facility f JOIN f.sports s WHERE s = :sportType")
    List<Facility> findBySportType(@Param("sportType") Sport sportType);

    /**
     * Obter todas as instalações de um owner.
     */
    List<Facility> findByOwnerId(Long ownerId);

    /**
     * Validar se a facility pertence ao owner.
     */
    Optional<Facility> findByIdAndOwnerId(Long facilityId, Long ownerId);

    List<Facility> findByName(String name);

    boolean existsByName(String name);
}
