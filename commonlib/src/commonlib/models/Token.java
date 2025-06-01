package commonlib.models;

import java.io.Serializable;
import java.util.UUID;

public class Token implements Serializable {
    private final String userId;
    private final UUID sessionId;

    public Token(String userId) {
        this.userId = userId;
        this.sessionId = UUID.randomUUID();
    }

    public String getUserId() {
        return userId;
    }

    public UUID getSessionId() {
        return sessionId;
    }
}
