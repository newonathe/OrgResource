package edu.orgresource.extended.identity;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import edu.orgresource.extended.security.C1RequestIdFilter;

@Service
public class C1AuditService {
    private final AuditEventRepository events;
    private final Clock clock;

    public C1AuditService(AuditEventRepository events, Clock clock) { this.events = events; this.clock = clock; }

    @Transactional(propagation = Propagation.MANDATORY)
    public void append(Long orgId, Long actorId, String action, String targetType, String targetId) {
        String requestId = UUID.randomUUID().toString();
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            Object value = attributes.getRequest().getAttribute(C1RequestIdFilter.ATTRIBUTE);
            if (value instanceof String text && !text.isBlank()) requestId = text;
        }
        events.save(new AuditEvent(orgId, actorId, action, targetType, targetId, "SUCCESS", requestId, now()));
    }

    public LocalDateTime now() { return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC); }
}
