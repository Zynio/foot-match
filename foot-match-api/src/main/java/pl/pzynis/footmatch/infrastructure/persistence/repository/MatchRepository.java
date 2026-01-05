package pl.pzynis.footmatch.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.pzynis.footmatch.domain.model.MatchStatus;
import pl.pzynis.footmatch.infrastructure.persistence.entity.MatchEntity;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<MatchEntity, UUID> {

    Page<MatchEntity> findByOrganizerId(UUID organizerId, Pageable pageable);

    Page<MatchEntity> findByStatus(MatchStatus status, Pageable pageable);

    @Query("SELECT m FROM MatchEntity m WHERE m.status = :status AND m.matchDate > :now ORDER BY m.matchDate ASC")
    Page<MatchEntity> findOpenUpcomingMatches(
            @Param("status") MatchStatus status,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Query("""
SELECT m FROM MatchEntity m
WHERE (:status IS NULL OR m.status = :status)
""")
    Page<MatchEntity> findWithFilters(
            @Param("status") MatchStatus status,
            @Param("location") String location,
            @Param("dateFrom") Instant dateFrom,
            Pageable pageable
    );

    @Query("SELECT COUNT(mp) FROM MatchParticipantEntity mp " +
            "WHERE mp.match.id = :matchId AND mp.status = 'ACCEPTED'")
    int countAcceptedParticipants(@Param("matchId") UUID matchId);
}
