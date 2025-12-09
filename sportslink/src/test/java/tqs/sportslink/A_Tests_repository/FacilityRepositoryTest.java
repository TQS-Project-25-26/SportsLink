package tqs.sportslink.A_Tests_repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Sport;
import tqs.sportslink.data.model.User;
import tqs.sportslink.data.FacilityRepository;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FacilityRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FacilityRepository facilityRepository;

    @Test
    void whenFindByCityAndSportType_thenReturnFacility() {
        // given
        Facility f = new Facility();
        f.setName("Padel Center");
        f.setCity("Aveiro");
        f.setAddress("Rua A");
        f.setStatus("ACTIVE");
        f.setPricePerHour(10.0);
        f.setLatitude(0.0);
        f.setLongitude(0.0);
        f.setOpeningTime(LocalTime.of(9, 0));
        f.setClosingTime(LocalTime.of(22, 0));
        f.setSports(List.of(Sport.PADEL));
        entityManager.persistAndFlush(f);

        // when
        List<Facility> found = facilityRepository.findByCityAndSportType("Aveiro", Sport.PADEL);

        // then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Padel Center");
    }

    @Test
    void whenFindByOwnerId_thenReturnFacilities() {
        // Create owner
        User owner = new User();
        owner.setEmail("owner@test.com");
        owner.setName("Owner");
        owner.setPassword("pass");
        entityManager.persistAndFlush(owner);

        // Create facility linked to owner
        Facility f = new Facility();
        f.setName("My Field");
        f.setCity("Lisboa");
        f.setAddress("Rua B");
        f.setStatus("ACTIVE");
        f.setPricePerHour(20.0);
        f.setLatitude(0.0);
        f.setLongitude(0.0);
        f.setOpeningTime(LocalTime.of(9, 0));
        f.setClosingTime(LocalTime.of(22, 0));
        f.setSports(List.of(Sport.FOOTBALL));
        f.setOwner(owner);
        entityManager.persistAndFlush(f);

        // when
        List<Facility> found = facilityRepository.findByOwnerId(owner.getId());

        // then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("My Field");
    }
}
