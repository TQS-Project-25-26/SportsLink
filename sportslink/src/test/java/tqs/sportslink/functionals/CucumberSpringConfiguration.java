package tqs.sportslink.functionals;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
    // Esta classe serve apenas para configurar o contexto Spring para os testes Cucumber
    // O Spring Boot vai iniciar a aplicação na porta 8080 (definida no application.properties)
}
