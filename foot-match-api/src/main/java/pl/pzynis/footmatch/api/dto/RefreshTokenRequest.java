package pl.pzynis.footmatch.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to refresh access token")
public record RefreshTokenRequest(
    @Schema(description = "JWT refresh token", example = "eyJhbGciOiJIUzI1NiIs...")
    @NotBlank(message = "Refresh token is required")
    String refreshToken
) {}
