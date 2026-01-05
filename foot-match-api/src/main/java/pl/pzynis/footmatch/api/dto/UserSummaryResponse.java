package pl.pzynis.footmatch.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Brief user information")
public record UserSummaryResponse(
    @Schema(description = "User unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "User display name", example = "Jan Kowalski")
    String name
) {}
