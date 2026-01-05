package pl.pzynis.footmatch.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.pzynis.footmatch.domain.model.UserRole;

@Schema(description = "Request to register a new user")
public record RegisterRequest(
    @Schema(description = "User email address", example = "jan@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @Schema(description = "User password (min 8 characters)", example = "securePassword123")
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password,

    @Schema(description = "User display name", example = "Jan Kowalski")
    @NotBlank(message = "Name is required")
    String name,

    @Schema(description = "User role", example = "PLAYER")
    @NotNull(message = "Role is required")
    UserRole role
) {}
