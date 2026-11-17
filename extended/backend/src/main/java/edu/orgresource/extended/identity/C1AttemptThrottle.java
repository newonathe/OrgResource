package edu.orgresource.extended.identity;

import java.time.Clock;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class C1AttemptThrottle {
    private record Bucket(long startedAt, int attempts) { }
    private final Clock clock;
    private final int capacity;
    private final long windowMillis;
    private final LinkedHashMap<String, Bucket> buckets = new LinkedHashMap<>(256, .75f, true);

    public C1AttemptThrottle(Clock clock,
            @Value("${c1.throttle.capacity:5}") int capacity,
            @Value("${c1.throttle.window:15m}") Duration window) {
        this.clock = clock; this.capacity = Math.max(1, capacity); this.windowMillis = window.toMillis();
    }

    public synchronized boolean allow(String key) {
        long now = clock.millis();
        Bucket existing = buckets.get(key);
        if (existing == null || now - existing.startedAt() >= windowMillis) {
            buckets.put(key, new Bucket(now, 1));
            trim(now);
            return true;
        }
        int attempts = existing.attempts() + 1;
        buckets.put(key, new Bucket(existing.startedAt(), attempts));
        trim(now);
        return attempts <= capacity;
    }

    public synchronized void success(String key) { buckets.remove(key); }

    public synchronized void resetForAcceptanceTests() { buckets.clear(); }

    private void trim(long now) {
        buckets.entrySet().removeIf(entry -> now - entry.getValue().startedAt() >= windowMillis);
        while (buckets.size() > 10000) {
            String oldest = buckets.keySet().iterator().next();
            buckets.remove(oldest);
        }
    }
}
