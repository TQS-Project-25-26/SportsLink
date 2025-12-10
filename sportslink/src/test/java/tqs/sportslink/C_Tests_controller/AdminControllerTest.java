package tqs.sportslink.C_Tests_controller;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tqs.sportslink.boundary.AdminController;
import tqs.sportslink.config.TestSecurityConfig;
import tqs.sportslink.data.model.User;
import tqs.sportslink.service.AdminService;
import tqs.sportslink.service.AuthService;
import tqs.sportslink.util.JwtUtil;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AdminController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@Requirement("SL-343")
class AdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private AuthService authService; // Security dependency

    @MockitoBean
    private JwtUtil jwtUtil; // Security dependency

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsers() throws Exception {
        User user = new User();
        user.setName("John");
        user.setEmail("john@test.com");
        given(adminService.getAllUsers()).willReturn(List.of(user));

        mvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUserStatus() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setActive(false);
        given(adminService.updateUserStatus(eq(1L), eq(false))).willReturn(user);

        mvc.perform(put("/api/admin/users/1/status?active=false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteFacility() throws Exception {
        mvc.perform(delete("/api/admin/facilities/1"))
                .andExpect(status().isNoContent());

        verify(adminService).deleteFacility(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetSystemStats() throws Exception {
        given(adminService.getSystemStats()).willReturn(Map.of("totalUsers", 10));

        mvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCancelRental() throws Exception {
        tqs.sportslink.data.model.Rental rental = new tqs.sportslink.data.model.Rental();
        rental.setId(1L);
        rental.setStatus("CANCELLED");

        given(adminService.cancelRental(1L)).willReturn(rental);

        mvc.perform(post("/api/admin/rentals/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(adminService).cancelRental(1L);
    }
}
