package pl.pzynis.footmatch.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.pzynis.footmatch.domain.model.ParticipantStatus;
import pl.pzynis.footmatch.infrastructure.persistence.entity.MatchParticipantEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchParticipantRepository extends JpaRepository<MatchParticipantEntity, UUID> {

    List<MatchParticipantEntity> findByMatchId(UUID matchId);

    List<MatchParticipantEntity> findByPlayerId(UUID playerId);

    Optional<MatchParticipantEntity> findByMatchIdAndPlayerId(UUID matchId, UUID playerId);

    boolean existsByMatchIdAndPlayerId(UUID matchId, UUID playerId);

    @Query("SELECT COUNT(mp) FROM MatchParticipantEntity mp " +
            "WHERE mp.match.id = :matchId AND mp.status = :status")
    int countByMatchIdAndStatus(@Param("matchId") UUID matchId, @Param("status") ParticipantStatus status);

    @Query("SELECT mp FROM MatchParticipantEntity mp " +
            "JOIN FETCH mp.player " +
            "WHERE mp.match.id = :matchId " +
            "ORDER BY mp.joinedAt ASC")
    List<MatchParticipantEntity> findByMatchIdWithPlayer(@Param("matchId") UUID matchId);
}
