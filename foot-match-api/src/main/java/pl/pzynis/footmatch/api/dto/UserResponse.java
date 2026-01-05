package pl.pzynis.footmatch.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.pzynis.footmatch.domain.model.UserRole;

import java.util.UUID;

@Schema(description = "User information response")
public record UserResponse(
    @Schema(description = "User unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "User email address", example = "jan@example.com")
    String email,

    @Schema(description = "User display name", example = "Jan Kowalski")
    String name,

    @Schema(description = "User role", example = "PLAYER")
    UserRole role
) {}
