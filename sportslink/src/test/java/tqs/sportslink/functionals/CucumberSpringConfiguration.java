package tqs.sportslink.functionals;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(tqs.sportslink.config.TestSecurityConfig.class)
public class CucumberSpringConfiguration {
    // Esta classe serve apenas para configurar o contexto Spring para os testes
    // Cucumber
    // O Spring Boot vai iniciar a aplicação em uma porta aleatória disponível
}
