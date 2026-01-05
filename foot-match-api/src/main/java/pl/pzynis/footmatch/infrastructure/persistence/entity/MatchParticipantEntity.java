package pl.pzynis.footmatch.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import pl.pzynis.footmatch.domain.model.ParticipantStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "match_participant",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_match_participant_match_player",
                columnNames = {"match_id", "player_id"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchParticipantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private MatchEntity match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private UserEntity player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ParticipantStatus status = ParticipantStatus.PENDING;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = Instant.now();
    }
}
