package tqs.sportslink.C_Tests_controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tqs.sportslink.boundary.RestExceptionHandler;
import tqs.sportslink.config.TestSecurityConfig;

import java.util.NoSuchElementException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TestController.class)
@Import({RestExceptionHandler.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class RestExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenIllegalArgument_thenReturn400() throws Exception {
        mockMvc.perform(get("/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid argument provided"));
    }

    @Test
    void whenNoSuchElement_thenReturn404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                // Expect the message from the exception, NOT "Recurso não encontrado" which is for static resources
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @Test
    void whenIllegalState_thenReturn409() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Conflict state detected"));
    }

    @Test
    void whenGenericException_thenReturn500() throws Exception {
        mockMvc.perform(get("/test/internal-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro inesperado"));
    }
}

@RestController
class TestController {
    @GetMapping("/test/bad-request")
    public void badRequest() {
        throw new IllegalArgumentException("Invalid argument provided");
    }

    @GetMapping("/test/not-found")
    public void notFound() {
        throw new NoSuchElementException("Resource not found");
    }

    @GetMapping("/test/conflict")
    public void conflict() {
        throw new IllegalStateException("Conflict state detected");
    }

    @GetMapping("/test/internal-error")
    public void internalError() {
        throw new RuntimeException("Unexpected error");
    }
}

