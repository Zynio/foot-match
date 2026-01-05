package pl.pzynis.footmatch.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Schema(description = "Request to update an existing match")
public record UpdateMatchRequest(
    @Schema(description = "Match title", example = "Mecz na orliku - zmiana")
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be at most 100 characters")
    String title,

    @Schema(description = "Match description", example = "Przyjdźcie w dobrych humorach!")
    @Size(max = 500, message = "Description must be at most 500 characters")
    String description,

    @Schema(description = "Match location", example = "Orlik Mokotów, ul. Puławska 12")
    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location must be at most 255 characters")
    String location,

    @Schema(description = "Match date and time", example = "2024-12-20T18:00:00")
    @NotNull(message = "Match date is required")
    @Future(message = "Match date must be in the future")
    LocalDateTime matchDate,

    @Schema(description = "Maximum number of players", example = "10", minimum = "2", maximum = "50")
    @Min(value = 2, message = "Minimum 2 players required")
    @Max(value = 50, message = "Maximum 50 players allowed")
    int maxPlayers
) {}
