package tqs.sportslink;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BasicHealthCheckTest {

    @Test
    void contextLoads() {
        assertTrue(true);           // passa sempre
    }

    @Test
    void matematicaBasica() {
        assertEquals(4, 2 + 2);
    }

    @Test
    void testeQueFalhaDeProposito() {   //para ver o relatorio de testes falhados
        assertEquals(5, 2 + 2);
    }
}