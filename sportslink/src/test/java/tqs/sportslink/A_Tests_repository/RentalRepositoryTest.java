package tqs.sportslink.A_Tests_repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Rental;
import tqs.sportslink.data.model.User;
import tqs.sportslink.data.RentalRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RentalRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RentalRepository rentalRepository;

    @Test
    void whenFindByFacilityId_thenReturnRentals() {
        // Setup User & Facility
        User u = new User();
        u.setEmail("renter@test.com");
        u.setName("Renter");
        u.setPassword("pass");
        entityManager.persistAndFlush(u);

        Facility f = new Facility();
        f.setName("Arena");
        f.setCity("Faro");
        f.setAddress("Rua D");
        f.setStatus("ACTIVE");
        f.setPricePerHour(10.0);
        f.setLatitude(0.0);
        f.setLongitude(0.0);
        f.setOpeningTime(LocalTime.of(8, 0));
        f.setClosingTime(LocalTime.of(22, 0));
        entityManager.persistAndFlush(f);

        // Create Rental
        Rental r = new Rental();
        r.setUser(u);
        r.setFacility(f);
        r.setStatus("CONFIRMED");
        r.setTotalPrice(20.0);
        r.setStartTime(LocalDateTime.of(2025, 10, 10, 10, 0));
        r.setEndTime(LocalDateTime.of(2025, 10, 10, 12, 0));
        entityManager.persistAndFlush(r);

        // Test findByFacilityId
        List<Rental> found = rentalRepository.findByFacilityId(f.getId());
        assertThat(found).hasSize(1);
    }
}
