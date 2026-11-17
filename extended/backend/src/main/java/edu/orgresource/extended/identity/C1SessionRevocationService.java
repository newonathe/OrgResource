package edu.orgresource.extended.identity;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

@Service
public class C1SessionRevocationService {
    private final FindByIndexNameSessionRepository<? extends Session> sessions;
    public C1SessionRevocationService(FindByIndexNameSessionRepository<? extends Session> sessions) {
        this.sessions = sessions;
    }

    public void revokeOthers(String usernameKey, String exceptSessionId) {
        Map<String, ? extends Session> found = sessions.findByPrincipalName(usernameKey);
        for (String id : found.keySet()) if (!id.equals(exceptSessionId)) sessions.deleteById(id);
    }

    public void revokeAll(String usernameKey) {
        for (String id : sessions.findByPrincipalName(usernameKey).keySet()) sessions.deleteById(id);
    }
}
