package pl.pzynis.footmatch.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pzynis.footmatch.api.dto.*;
import pl.pzynis.footmatch.domain.exception.*;
import pl.pzynis.footmatch.domain.model.MatchStatus;
import pl.pzynis.footmatch.domain.model.ParticipantStatus;
import pl.pzynis.footmatch.infrastructure.persistence.entity.MatchEntity;
import pl.pzynis.footmatch.infrastructure.persistence.entity.MatchParticipantEntity;
import pl.pzynis.footmatch.infrastructure.persistence.entity.UserEntity;
import pl.pzynis.footmatch.infrastructure.persistence.repository.MatchParticipantRepository;
import pl.pzynis.footmatch.infrastructure.persistence.repository.MatchRepository;
import pl.pzynis.footmatch.infrastructure.persistence.repository.UserRepository;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchParticipantRepository participantRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<MatchResponse> findAll(MatchStatus status, String location, Instant dateFrom, Pageable pageable) {
        return matchRepository.findWithFilters(status, location, dateFrom, pageable)
                .map(this::toMatchResponse);
    }

    @Transactional(readOnly = true)
    public MatchResponse findById(UUID matchId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
        return toMatchResponse(match);
    }

    public MatchResponse create(CreateMatchRequest request, UUID organizerId) {
        UserEntity organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new RuntimeException("User not found: " + organizerId));

        MatchEntity match = MatchEntity.builder()
                .organizer(organizer)
                .title(request.title())
                .description(request.description())
                .location(request.location())
                .matchDate(request.matchDate().toInstant(ZoneOffset.UTC))
                .maxPlayers(request.maxPlayers())
                .status(MatchStatus.OPEN)
                .build();

        MatchEntity saved = matchRepository.save(match);
        return toMatchResponse(saved);
    }

    public MatchResponse update(UUID matchId, UpdateMatchRequest request, UUID userId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));

        if (!match.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Only the organizer can update this match");
        }

        match.setTitle(request.title());
        match.setDescription(request.description());
        match.setLocation(request.location());
        match.setMatchDate(request.matchDate().toInstant(ZoneOffset.UTC));
        match.setMaxPlayers(request.maxPlayers());

        MatchEntity saved = matchRepository.save(match);
        return toMatchResponse(saved);
    }

    public void delete(UUID matchId, UUID userId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));

        if (!match.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Only the organizer can delete this match");
        }

        matchRepository.delete(match);
    }

    public void cancelMatch(UUID matchId, UUID userId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));

        if (!match.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Only the organizer can cancel this match");
        }

        match.setStatus(MatchStatus.CANCELLED);
        matchRepository.save(match);
    }

    public ParticipantResponse joinMatch(UUID matchId, UUID playerId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));

        if (match.getStatus() != MatchStatus.OPEN) {
            throw new RuntimeException("Cannot join a match that is not open");
        }

        if (match.getOrganizer().getId().equals(playerId)) {
            throw new RuntimeException("Organizer cannot join their own match");
        }

        if (participantRepository.existsByMatchIdAndPlayerId(matchId, playerId)) {
            throw new AlreadyJoinedException(matchId, playerId);
        }

        int currentParticipants = participantRepository.countByMatchIdAndStatus(matchId, ParticipantStatus.ACCEPTED);
        if (currentParticipants >= match.getMaxPlayers()) {
            throw new MatchFullException(matchId);
        }

        UserEntity player = userRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("User not found: " + playerId));

        MatchParticipantEntity participant = MatchParticipantEntity.builder()
                .match(match)
                .player(player)
                .status(ParticipantStatus.PENDING)
                .build();

        MatchParticipantEntity saved = participantRepository.save(participant);
        return toParticipantResponse(saved);
    }

    public void leaveMatch(UUID matchId, UUID playerId) {
        MatchParticipantEntity participant = participantRepository.findByMatchIdAndPlayerId(matchId, playerId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        participantRepository.delete(participant);
    }

    @Transactional(readOnly = true)
    public List<ParticipantResponse> getParticipants(UUID matchId) {
        if (!matchRepository.existsById(matchId)) {
            throw new MatchNotFoundException(matchId);
        }

        return participantRepository.findByMatchIdWithPlayer(matchId).stream()
                .map(this::toParticipantResponse)
                .toList();
    }

    public ParticipantResponse updateParticipantStatus(UUID matchId, UUID playerId, ParticipantStatus status, UUID organizerId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));

        if (!match.getOrganizer().getId().equals(organizerId)) {
            throw new UnauthorizedAccessException("Only the organizer can update participant status");
        }

        MatchParticipantEntity participant = participantRepository.findByMatchIdAndPlayerId(matchId, playerId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        if (status == ParticipantStatus.ACCEPTED) {
            int currentAccepted = participantRepository.countByMatchIdAndStatus(matchId, ParticipantStatus.ACCEPTED);
            if (currentAccepted >= match.getMaxPlayers()) {
                throw new MatchFullException(matchId);
            }
        }

        participant.setStatus(status);
        MatchParticipantEntity saved = participantRepository.save(participant);

        // Auto-close match if full
        int newAccepted = participantRepository.countByMatchIdAndStatus(matchId, ParticipantStatus.ACCEPTED);
        if (newAccepted >= match.getMaxPlayers()) {
            match.setStatus(MatchStatus.CLOSED);
            matchRepository.save(match);
        }

        return toParticipantResponse(saved);
    }

    private MatchResponse toMatchResponse(MatchEntity entity) {
        int currentPlayers = participantRepository.countByMatchIdAndStatus(entity.getId(), ParticipantStatus.ACCEPTED);

        return new MatchResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getLocation(),
                entity.getMatchDate(),
                entity.getMaxPlayers(),
                currentPlayers,
                entity.getStatus(),
                new UserSummaryResponse(
                        entity.getOrganizer().getId(),
                        entity.getOrganizer().getName()
                ),
                entity.getCreatedAt()
        );
    }

    private ParticipantResponse toParticipantResponse(MatchParticipantEntity entity) {
        return new ParticipantResponse(
                entity.getId(),
                new UserSummaryResponse(
                        entity.getPlayer().getId(),
                        entity.getPlayer().getName()
                ),
                entity.getStatus(),
                entity.getJoinedAt()
        );
    }
}
