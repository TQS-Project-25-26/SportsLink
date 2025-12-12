package tqs.sportslink;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@org.springframework.context.annotation.Import(tqs.sportslink.config.TestSecurityConfig.class)
class SportslinkApplicationTests {

	@Test
	void contextLoads() {
	}

}
