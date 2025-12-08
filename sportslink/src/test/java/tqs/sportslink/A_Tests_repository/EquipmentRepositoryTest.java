package tqs.sportslink.A_Tests_repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tqs.sportslink.data.model.Equipment;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.EquipmentRepository;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EquipmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Test
    void whenFindByFacilityId_thenReturnEquipment() {
        // given
        Facility f = new Facility();
        f.setName("Gym");
        f.setCity("Porto");
        f.setAddress("Rua C");
        f.setStatus("ACTIVE");
        f.setPricePerHour(15.0);
        f.setLatitude(0.0);
        f.setLongitude(0.0);
        f.setOpeningTime(LocalTime.of(8, 0));
        f.setClosingTime(LocalTime.of(23, 0));
        entityManager.persistAndFlush(f);

        Equipment e = new Equipment();
        e.setName("Ball");
        e.setType("Football");
        e.setPricePerHour(2.0);
        e.setQuantity(5);
        e.setStatus("AVAILABLE");
        e.setFacility(f);
        entityManager.persistAndFlush(e);

        // when
        List<Equipment> found = equipmentRepository.findByFacilityId(f.getId());

        // then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Ball");
    }
}
