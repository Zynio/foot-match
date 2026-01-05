package pl.pzynis.footmatch.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.pzynis.footmatch.api.dto.CreateMatchRequest;
import pl.pzynis.footmatch.api.dto.MatchResponse;
import pl.pzynis.footmatch.api.dto.ParticipantResponse;
import pl.pzynis.footmatch.api.dto.UpdateMatchRequest;
import pl.pzynis.footmatch.domain.exception.AlreadyJoinedException;
import pl.pzynis.footmatch.domain.exception.MatchFullException;
import pl.pzynis.footmatch.domain.exception.MatchNotFoundException;
import pl.pzynis.footmatch.domain.exception.UnauthorizedAccessException;
import pl.pzynis.footmatch.domain.model.MatchStatus;
import pl.pzynis.footmatch.domain.model.ParticipantStatus;
import pl.pzynis.footmatch.domain.model.UserRole;
import pl.pzynis.footmatch.infrastructure.persistence.entity.MatchEntity;
import pl.pzynis.footmatch.infrastructure.persistence.entity.MatchParticipantEntity;
import pl.pzynis.footmatch.infrastructure.persistence.entity.UserEntity;
import pl.pzynis.footmatch.infrastructure.persistence.repository.MatchParticipantRepository;
import pl.pzynis.footmatch.infrastructure.persistence.repository.MatchRepository;
import pl.pzynis.footmatch.infrastructure.persistence.repository.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchService")
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchParticipantRepository participantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MatchService matchService;

    private static final UUID MATCH_ID = UUID.randomUUID();
    private static final UUID ORGANIZER_ID = UUID.randomUUID();
    private static final UUID PLAYER_ID = UUID.randomUUID();

    private UserEntity organizer;
    private UserEntity player;
    private MatchEntity match;

    @BeforeEach
    void setUp() {
        organizer = UserEntity.builder()
                .id(ORGANIZER_ID)
                .email("organizer@example.com")
                .name("Organizator")
                .role(UserRole.ORGANIZER)
                .build();

        player = UserEntity.builder()
                .id(PLAYER_ID)
                .email("player@example.com")
                .name("Gracz")
                .role(UserRole.PLAYER)
                .build();

        match = MatchEntity.builder()
                .id(MATCH_ID)
                .title("Mecz testowy")
                .description("Opis meczu")
                .location("Orlik Mokotow")
                .matchDate(Instant.now().plusSeconds(86400))
                .maxPlayers(10)
                .status(MatchStatus.OPEN)
                .organizer(organizer)
                .build();
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTests {

        @Test
        @DisplayName("powinien zwrócić mecz gdy istnieje")
        void shouldReturnMatchWhenExists() {
            // given
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
            when(participantRepository.countByMatchIdAndStatus(MATCH_ID, ParticipantStatus.ACCEPTED)).thenReturn(5);

            // when
            MatchResponse response = matchService.findById(MATCH_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(MATCH_ID);
            assertThat(response.title()).isEqualTo("Mecz testowy");
            assertThat(response.location()).isEqualTo("Orlik Mokotow");
            assertThat(response.currentPlayers()).isEqualTo(5);
            assertThat(response.organizer().id()).isEqualTo(ORGANIZER_ID);
        }

        @Test
        @DisplayName("powinien rzucić wyjątek gdy mecz nie istnieje")
        void shouldThrowWhenMatchNotFound() {
            // given
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> matchService.findById(MATCH_ID))
                    .isInstanceOf(MatchNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("powinien utworzyć nowy mecz")
        void shouldCreateMatch() {
            // given
            CreateMatchRequest request = new CreateMatchRequest(
                    "Nowy mecz",
                    "Opis",
                    "Orlik Ursynow",
                    LocalDateTime.now().plusDays(1),
                    10
            );

            when(userRepository.findById(ORGANIZER_ID)).thenReturn(Optional.of(organizer));
            when(matchRepository.save(any(MatchEntity.class))).thenAnswer(inv -> {
                MatchEntity saved = inv.getArgument(0);
                saved.setId(MATCH_ID);
                return saved;
            });
            when(participantRepository.countByMatchIdAndStatus(any(), any())).thenReturn(0);

            // when
            MatchResponse response = matchService.create(request, ORGANIZER_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.title()).isEqualTo("Nowy mecz");
            assertThat(response.status()).isEqualTo(MatchStatus.OPEN);

            verify(matchRepository).save(any(MatchEntity.class));
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("powinien zaktualizować mecz przez organizatora")
        void shouldUpdateMatchByOrganizer() {
            // given
            UpdateMatchRequest request = new UpdateMatchRequest(
                    "Zaktualizowany tytul",
                    "Nowy opis",
                    "Nowa lokalizacja",
                    LocalDateTime.now().plusDays(2),
                    12
            );

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
            when(matchRepository.save(any(MatchEntity.class))).thenReturn(match);
            when(participantRepository.countByMatchIdAndStatus(MATCH_ID, ParticipantStatus.ACCEPTED)).thenReturn(3);

            // when
            MatchResponse response = matchService.update(MATCH_ID, request, ORGANIZER_ID);

            // then
            assertThat(response).isNotNull();
            verify(matchRepository).save(any(MatchEntity.class));
        }

        @Test
        @DisplayName("powinien rzucić wyjątek gdy nie-organizator próbuje zaktualizować")
        void shouldThrowWhenNonOrganizerUpdates() {
            // given
            UpdateMatchRequest request = new UpdateMatchRequest(
                    "Tytul", "Opis", "Lokalizacja", LocalDateTime.now().plusDays(1), 10
            );

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));

            // when/then
            assertThatThrownBy(() -> matchService.update(MATCH_ID, request, PLAYER_ID))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessageContaining("Only the organizer");
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("powinien usunąć mecz przez organizatora")
        void shouldDeleteMatchByOrganizer() {
            // given
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));

            // when
            matchService.delete(MATCH_ID, ORGANIZER_ID);

            // then
            verify(matchRepository).delete(match);
        }

        @Test
        @DisplayName("powinien rzucić wyjątek gdy nie-organizator próbuje usunąć")
        void shouldThrowWhenNonOrganizerDeletes() {
            // given
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));

            // when/then
            assertThatThrownBy(() -> matchService.delete(MATCH_ID, PLAYER_ID))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }

    @Nested
    @DisplayName("joinMatch()")
    class JoinMatchTests {

        @Test
        @DisplayName("powinien pozwolić graczowi dołączyć do meczu")
        void shouldAllowPlayerToJoin() {
            // given
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
            when(participantRepository.existsByMatchIdAndPlayerId(MATCH_ID, PLAYER_ID)).thenReturn(false);
            when(participantRepository.countByMatchIdAndStatus(MATCH_ID, ParticipantStatus.ACCEPTED)).thenReturn(5);
            when(userRepository.findById(PLAYER_ID)).thenReturn(Optional.of(player));
            when(participantRepository.save(any(MatchParticipantEntity.class))).thenAnswer(inv -> {
                MatchParticipantEntity saved = inv.getArgument(0);
                saved.setId(UUID.randomUUID());
                return saved;
            });

            // when
            ParticipantResponse response = matchService.joinMatch(MATCH_ID, PLAYER_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo(ParticipantStatus.PENDING);
            assertThat(response.player().id()).isEqualTo(PLAYER_ID);

            verify(participantRepository).save(any(MatchParticipantEntity.class));
        }

        @Test
        @DisplayName("powinien rzucić wyjątek gdy mecz jest pełny")
        void shouldThrowWhenMatchIsFull() {
            // given
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
            when(participantRepository.existsByMatchIdAndPlayerId(MATCH_ID, PLAYER_ID)).thenReturn(false);
            when(participantRepository.countByMatchIdAndStatus(MATCH_ID, ParticipantStatus.ACCEPTED)).thenReturn(10);

            // when/then
            assertThatThrownBy(() -> matchService.joinMatch(MATCH_ID, PLAYER_ID))
                    .isInstanceOf(MatchFullException.class);
        }

        @Test
        @DisplayName("powinien rzucić wyjątek gdy gracz już dołączył")
        void shouldThrowWhenAlreadyJoined() {
            // given
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
            when(participantRepository.existsByMatchIdAndPlayerId(MATCH_ID, PLAYER_ID)).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> matchService.joinMatch(MATCH_ID, PLAYER_ID))
                    .isInstanceOf(AlreadyJoinedException.class);
        }

        @Test
        @DisplayName("powinien rzucić wyjątek gdy organizator próbuje dołączyć do własnego meczu")
        void shouldThrowWhenOrganizerJoinsOwnMatch() {
            // given
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));

            // when/then
            assertThatThrownBy(() -> matchService.joinMatch(MATCH_ID, ORGANIZER_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Organizer cannot join");
        }

        @Test
        @DisplayName("powinien rzucić wyjątek gdy mecz nie jest otwarty")
        void shouldThrowWhenMatchNotOpen() {
            // given
            match.setStatus(MatchStatus.CLOSED);
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));

            // when/then
            assertThatThrownBy(() -> matchService.joinMatch(MATCH_ID, PLAYER_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not open");
        }
    }

    @Nested
    @DisplayName("leaveMatch()")
    class LeaveMatchTests {

        @Test
        @DisplayName("powinien pozwolić graczowi opuścić mecz")
        void shouldAllowPlayerToLeave() {
            // given
            MatchParticipantEntity participant = MatchParticipantEntity.builder()
                    .id(UUID.randomUUID())
                    .match(match)
                    .player(player)
                    .status(ParticipantStatus.PENDING)
                    .build();

            when(participantRepository.findByMatchIdAndPlayerId(MATCH_ID, PLAYER_ID))
                    .thenReturn(Optional.of(participant));

            // when
            matchService.leaveMatch(MATCH_ID, PLAYER_ID);

            // then
            verify(participantRepository).delete(participant);
        }

        @Test
        @DisplayName("powinien rzucić wyjątek gdy uczestnik nie istnieje")
        void shouldThrowWhenParticipantNotFound() {
            // given
            when(participantRepository.findByMatchIdAndPlayerId(MATCH_ID, PLAYER_ID))
                    .thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> matchService.leaveMatch(MATCH_ID, PLAYER_ID))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("updateParticipantStatus()")
    class UpdateParticipantStatusTests {

        @Test
        @DisplayName("powinien zaakceptować uczestnika")
        void shouldAcceptParticipant() {
            // given
            MatchParticipantEntity participant = MatchParticipantEntity.builder()
                    .id(UUID.randomUUID())
                    .match(match)
                    .player(player)
                    .status(ParticipantStatus.PENDING)
                    .build();

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
            when(participantRepository.findByMatchIdAndPlayerId(MATCH_ID, PLAYER_ID))
                    .thenReturn(Optional.of(participant));
            when(participantRepository.countByMatchIdAndStatus(MATCH_ID, ParticipantStatus.ACCEPTED))
                    .thenReturn(5);
            when(participantRepository.save(any(MatchParticipantEntity.class))).thenReturn(participant);

            // when
            ParticipantResponse response = matchService.updateParticipantStatus(
                    MATCH_ID, PLAYER_ID, ParticipantStatus.ACCEPTED, ORGANIZER_ID
            );

            // then
            assertThat(response).isNotNull();
            verify(participantRepository).save(any(MatchParticipantEntity.class));
        }

        @Test
        @DisplayName("powinien odrzucić uczestnika")
        void shouldRejectParticipant() {
            // given
            MatchParticipantEntity participant = MatchParticipantEntity.builder()
                    .id(UUID.randomUUID())
                    .match(match)
                    .player(player)
                    .status(ParticipantStatus.PENDING)
                    .build();

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
            when(participantRepository.findByMatchIdAndPlayerId(MATCH_ID, PLAYER_ID))
                    .thenReturn(Optional.of(participant));
            when(participantRepository.countByMatchIdAndStatus(MATCH_ID, ParticipantStatus.ACCEPTED))
                    .thenReturn(5);
            when(participantRepository.save(any(MatchParticipantEntity.class))).thenReturn(participant);

            // when
            ParticipantResponse response = matchService.updateParticipantStatus(
                    MATCH_ID, PLAYER_ID, ParticipantStatus.REJECTED, ORGANIZER_ID
            );

            // then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("powinien automatycznie zamknąć mecz gdy jest pełny")
        void shouldAutoCloseMatchWhenFull() {
            // given
            match.setMaxPlayers(10);
            MatchParticipantEntity participant = MatchParticipantEntity.builder()
                    .id(UUID.randomUUID())
                    .match(match)
                    .player(player)
                    .status(ParticipantStatus.PENDING)
                    .build();

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
            when(participantRepository.findByMatchIdAndPlayerId(MATCH_ID, PLAYER_ID))
                    .thenReturn(Optional.of(participant));
            when(participantRepository.countByMatchIdAndStatus(MATCH_ID, ParticipantStatus.ACCEPTED))
                    .thenReturn(9)  // przed akceptacja
                    .thenReturn(10); // po akceptacji - teraz pełny
            when(participantRepository.save(any(MatchParticipantEntity.class))).thenReturn(participant);

            // when
            matchService.updateParticipantStatus(MATCH_ID, PLAYER_ID, ParticipantStatus.ACCEPTED, ORGANIZER_ID);

            // then
            verify(matchRepository).save(match);
            assertThat(match.getStatus()).isEqualTo(MatchStatus.CLOSED);
        }

        @Test
        @DisplayName("powinien rzucić wyjątek gdy nie-organizator aktualizuje status")
        void shouldThrowWhenNonOrganizerUpdatesStatus() {
            // given
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));

            // when/then
            assertThatThrownBy(() -> matchService.updateParticipantStatus(
                    MATCH_ID, PLAYER_ID, ParticipantStatus.ACCEPTED, PLAYER_ID
            ))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }

        @Test
        @DisplayName("powinien rzucić wyjątek przy akceptacji gdy mecz jest pełny")
        void shouldThrowWhenAcceptingButMatchFull() {
            // given
            MatchParticipantEntity participant = MatchParticipantEntity.builder()
                    .id(UUID.randomUUID())
                    .match(match)
                    .player(player)
                    .status(ParticipantStatus.PENDING)
                    .build();

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
            when(participantRepository.findByMatchIdAndPlayerId(MATCH_ID, PLAYER_ID))
                    .thenReturn(Optional.of(participant));
            when(participantRepository.countByMatchIdAndStatus(MATCH_ID, ParticipantStatus.ACCEPTED))
                    .thenReturn(10); // już pełny

            // when/then
            assertThatThrownBy(() -> matchService.updateParticipantStatus(
                    MATCH_ID, PLAYER_ID, ParticipantStatus.ACCEPTED, ORGANIZER_ID
            ))
                    .isInstanceOf(MatchFullException.class);
        }
    }

    @Nested
    @DisplayName("getParticipants()")
    class GetParticipantsTests {

        @Test
        @DisplayName("powinien zwrócić listę uczestników")
        void shouldReturnParticipantsList() {
            // given
            MatchParticipantEntity participant = MatchParticipantEntity.builder()
                    .id(UUID.randomUUID())
                    .match(match)
                    .player(player)
                    .status(ParticipantStatus.ACCEPTED)
                    .joinedAt(Instant.now())
                    .build();

            when(matchRepository.existsById(MATCH_ID)).thenReturn(true);
            when(participantRepository.findByMatchIdWithPlayer(MATCH_ID))
                    .thenReturn(List.of(participant));

            // when
            List<ParticipantResponse> participants = matchService.getParticipants(MATCH_ID);

            // then
            assertThat(participants).hasSize(1);
            assertThat(participants.get(0).player().id()).isEqualTo(PLAYER_ID);
            assertThat(participants.get(0).status()).isEqualTo(ParticipantStatus.ACCEPTED);
        }

        @Test
        @DisplayName("powinien rzucić wyjątek gdy mecz nie istnieje")
        void shouldThrowWhenMatchNotExists() {
            // given
            when(matchRepository.existsById(MATCH_ID)).thenReturn(false);

            // when/then
            assertThatThrownBy(() -> matchService.getParticipants(MATCH_ID))
                    .isInstanceOf(MatchNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("cancelMatch()")
    class CancelMatchTests {

        @Test
        @DisplayName("powinien anulować mecz przez organizatora")
        void shouldCancelMatchByOrganizer() {
            // given
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));

            // when
            matchService.cancelMatch(MATCH_ID, ORGANIZER_ID);

            // then
            assertThat(match.getStatus()).isEqualTo(MatchStatus.CANCELLED);
            verify(matchRepository).save(match);
        }

        @Test
        @DisplayName("powinien rzucić wyjątek gdy nie-organizator anuluje")
        void shouldThrowWhenNonOrganizerCancels() {
            // given
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));

            // when/then
            assertThatThrownBy(() -> matchService.cancelMatch(MATCH_ID, PLAYER_ID))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }
}
