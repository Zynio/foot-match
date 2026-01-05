package pl.pzynis.footmatch.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Error response")
public record ErrorResponse(
    @Schema(description = "Error code", example = "VALIDATION_ERROR")
    String code,

    @Schema(description = "Error message", example = "email: must not be blank")
    String message,

    @Schema(description = "Error timestamp", example = "2024-12-15T10:30:00Z")
    Instant timestamp
) {
    public ErrorResponse(String code, String message) {
        this(code, message, Instant.now());
    }
}
