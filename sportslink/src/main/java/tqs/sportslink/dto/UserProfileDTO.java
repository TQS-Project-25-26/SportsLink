package tqs.sportslink.dto;

import java.time.LocalDateTime;

import tqs.sportslink.data.model.Role;

public record UserProfileDTO(
    Long id,
    String email,
    String name,
    String phone,
    Role role,
    Boolean active,
    int rentalsCount,
    int facilitiesCount,
    LocalDateTime createdAt
) {}
