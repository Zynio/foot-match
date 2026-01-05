package pl.pzynis.footmatch.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.pzynis.footmatch.domain.model.ParticipantStatus;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Match participant information")
public record ParticipantResponse(
    @Schema(description = "Participant record unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "Player information")
    UserSummaryResponse player,

    @Schema(description = "Participant status", example = "ACCEPTED")
    ParticipantStatus status,

    @Schema(description = "Date when player joined the match")
    Instant joinedAt
) {}
