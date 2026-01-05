package pl.pzynis.footmatch.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.pzynis.footmatch.domain.model.MatchStatus;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Match information response")
public record MatchResponse(
    @Schema(description = "Match unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "Match title", example = "Mecz na orliku")
    String title,

    @Schema(description = "Match description", example = "Przyjdźcie w dobrych humorach!")
    String description,

    @Schema(description = "Match location", example = "Orlik Mokotów, ul. Puławska 12")
    String location,

    @Schema(description = "Match date and time")
    Instant matchDate,

    @Schema(description = "Maximum number of players", example = "10")
    int maxPlayers,

    @Schema(description = "Current number of accepted players", example = "6")
    int currentPlayers,

    @Schema(description = "Match status", example = "OPEN")
    MatchStatus status,

    @Schema(description = "Match organizer information")
    UserSummaryResponse organizer,

    @Schema(description = "Match creation timestamp")
    Instant createdAt
) {}
