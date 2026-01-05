package pl.pzynis.footmatch.domain.exception;

import java.util.UUID;

public class AlreadyJoinedException extends RuntimeException {
    public AlreadyJoinedException(UUID matchId, UUID playerId) {
        super("Player " + playerId + " has already joined match " + matchId);
    }
}
