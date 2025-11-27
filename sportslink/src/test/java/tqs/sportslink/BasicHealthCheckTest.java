package tqs.sportslink;

import org.junit.jupiter.api.Test;

import app.getxray.xray.junit.customjunitxml.annotations.XrayTest;

import static org.junit.jupiter.api.Assertions.*;

@XrayTest
public class BasicHealthCheckTest {

    @Test
    void contextLoads() {
        assertTrue(true);           // passa sempre
    }

    @Test
    void matematicaBasica() {
        assertEquals(4, 2 + 2);
    }

}