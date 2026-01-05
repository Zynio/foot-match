package pl.pzynis.footmatch.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import pl.pzynis.footmatch.domain.model.ParticipantStatus;

@Schema(description = "Request to update participant status")
public record UpdateParticipantStatusRequest(
    @Schema(description = "New participant status", example = "ACCEPTED")
    @NotNull(message = "Status is required")
    ParticipantStatus status
) {}
