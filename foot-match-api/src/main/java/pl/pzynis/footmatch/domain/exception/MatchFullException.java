package pl.pzynis.footmatch.domain.exception;

import java.util.UUID;

public class MatchFullException extends RuntimeException {
    public MatchFullException(UUID matchId) {
        super("Match has reached maximum number of players: " + matchId);
    }
}
