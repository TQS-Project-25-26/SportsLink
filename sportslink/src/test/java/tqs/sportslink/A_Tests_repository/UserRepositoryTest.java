package tqs.sportslink.A_Tests_repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tqs.sportslink.data.model.User;
import tqs.sportslink.data.model.Role;
import tqs.sportslink.data.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void whenFindByEmail_thenReturnUser() {
        // given
        User user = new User();
        user.setEmail("alex@example.com");
        user.setPassword("password");
        user.setName("alex");
        user.setPhone("123456789");
        user.getRoles().add(Role.RENTER);
        entityManager.persistAndFlush(user);

        // when
        User found = userRepository.findByEmail(user.getEmail()).orElse(null);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void whenInvalidEmail_thenReturnNull() {
        User fromDb = userRepository.findByEmail("doesnotexist@email.com").orElse(null);
        assertThat(fromDb).isNull();
    }

    @Test
    void whenExistsByEmail_thenReturnTrue() {
        User user = new User();
        user.setEmail("bob@example.com");
        user.setPassword("pass");
        user.setName("bob");
        entityManager.persistAndFlush(user);

        assertThat(userRepository.existsByEmail("bob@example.com")).isTrue();
    }

    @Test
    void whenFindByRolesContaining_thenReturnUser() {
        User user = new User();
        user.setEmail("owner@example.com");
        user.setPassword("pass");
        user.setName("Owner");
        user.getRoles().add(Role.OWNER);
        entityManager.persistAndFlush(user);

        List<User> owners = userRepository.findByRolesContaining(Role.OWNER);
        assertThat(owners).hasSize(1);
        assertThat(owners.get(0).getName()).isEqualTo("Owner");
    }
}
