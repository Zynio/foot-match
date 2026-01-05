package pl.pzynis.footmatch.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response with JWT tokens")
public record AuthResponse(
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIs...")
    String accessToken,

    @Schema(description = "JWT refresh token", example = "eyJhbGciOiJIUzI1NiIs...")
    String refreshToken,

    @Schema(description = "Token type", example = "Bearer")
    String tokenType,

    @Schema(description = "Access token expiration time in seconds", example = "3600")
    long expiresIn,

    @Schema(description = "Authenticated user information")
    UserResponse user
) {
    public AuthResponse(String accessToken, String refreshToken, long expiresIn, UserResponse user) {
        this(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}
