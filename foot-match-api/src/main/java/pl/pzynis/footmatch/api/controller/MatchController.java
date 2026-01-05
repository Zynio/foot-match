package pl.pzynis.footmatch.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.pzynis.footmatch.api.dto.*;
import pl.pzynis.footmatch.application.service.MatchService;
import pl.pzynis.footmatch.domain.model.MatchStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Tag(name = "Matches", description = "Match management API")
public class MatchController {

    private final MatchService matchService;

    @Operation(
            summary = "Get all matches",
            description = "Returns a paginated list of matches with optional filtering"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matches retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<MatchResponse>> getMatches(
            @Parameter(description = "Filter by match status")
            @RequestParam(required = false) MatchStatus status,

            @Parameter(description = "Filter by location (partial match)")
            @RequestParam(required = false) String location,

            @Parameter(description = "Filter matches from this date onwards")
            @RequestParam(required = false) Instant dateFrom,

            @PageableDefault(size = 20, sort = "matchDate", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        Page<MatchResponse> matches = matchService.findAll(status, location, dateFrom, pageable);
        return ResponseEntity.ok(matches);
    }

    @Operation(
            summary = "Get match by ID",
            description = "Returns detailed information about a specific match"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Match found"),
            @ApiResponse(responseCode = "404", description = "Match not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<MatchResponse> getMatch(
            @Parameter(description = "Match ID")
            @PathVariable UUID id
    ) {
        MatchResponse match = matchService.findById(id);
        return ResponseEntity.ok(match);
    }

    @Operation(
            summary = "Create a new match",
            description = "Creates a new match. Only organizers can create matches."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Match created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - not an organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<MatchResponse> createMatch(
            @Valid @RequestBody CreateMatchRequest request,
            @AuthenticationPrincipal UUID userId
    ) {
        MatchResponse match = matchService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(match);
    }

    @Operation(
            summary = "Update a match",
            description = "Updates an existing match. Only the match organizer can update it."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Match updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Match not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{id}")
    public ResponseEntity<MatchResponse> updateMatch(
            @Parameter(description = "Match ID")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMatchRequest request,
            @AuthenticationPrincipal UUID userId
    ) {
        MatchResponse match = matchService.update(id, request, userId);
        return ResponseEntity.ok(match);
    }

    @Operation(
            summary = "Delete a match",
            description = "Deletes a match. Only the match organizer can delete it."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Match deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Match not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatch(
            @Parameter(description = "Match ID")
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        matchService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Join a match",
            description = "Join a match as a participant. Status will be PENDING until organizer accepts."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Successfully joined the match"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Match not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Already joined or match is full",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{id}/join")
    public ResponseEntity<ParticipantResponse> joinMatch(
            @Parameter(description = "Match ID")
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        ParticipantResponse participant = matchService.joinMatch(id, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(participant);
    }

    @Operation(
            summary = "Leave a match",
            description = "Leave a match that you have joined"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Successfully left the match"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Match or participant not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}/leave")
    public ResponseEntity<Void> leaveMatch(
            @Parameter(description = "Match ID")
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        matchService.leaveMatch(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get match participants",
            description = "Returns list of all participants for a match"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Participants retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Match not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantResponse>> getParticipants(
            @Parameter(description = "Match ID")
            @PathVariable UUID id
    ) {
        List<ParticipantResponse> participants = matchService.getParticipants(id);
        return ResponseEntity.ok(participants);
    }

    @Operation(
            summary = "Update participant status",
            description = "Accept or reject a participant. Only the match organizer can update status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Participant status updated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Match or participant not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Match is full",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{id}/participants/{playerId}")
    public ResponseEntity<ParticipantResponse> updateParticipantStatus(
            @Parameter(description = "Match ID")
            @PathVariable UUID id,

            @Parameter(description = "Player ID")
            @PathVariable UUID playerId,

            @Valid @RequestBody UpdateParticipantStatusRequest request,
            @AuthenticationPrincipal UUID userId
    ) {
        ParticipantResponse participant = matchService.updateParticipantStatus(id, playerId, request.status(), userId);
        return ResponseEntity.ok(participant);
    }
}
