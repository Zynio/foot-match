package pl.pzynis.footmatch.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to login a user")
public record LoginRequest(
    @Schema(description = "User email address", example = "jan@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @Schema(description = "User password", example = "securePassword123")
    @NotBlank(message = "Password is required")
    String password
) {}
