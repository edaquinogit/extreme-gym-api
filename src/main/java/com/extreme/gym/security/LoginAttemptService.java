package com.extreme.gym.security;

import com.extreme.gym.exception.LoginRateLimitException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    private static final String UNKNOWN_LOGIN = "unknown";
    private static final String UNKNOWN_IP = "unknown";

    private final ConcurrentMap<String, AttemptState> attempts = new ConcurrentHashMap<>();
    private final Clock clock;
    private final int maxFailures;
    private final Duration lockDuration;

    public LoginAttemptService(
            Clock clock,
            @Value("${app.auth.max-failed-attempts:5}") int maxFailures,
            @Value("${app.auth.lock-duration-minutes:15}") long lockDurationMinutes
    ) {
        this.clock = clock;
        this.maxFailures = maxFailures;
        this.lockDuration = Duration.ofMinutes(lockDurationMinutes);
    }

    public void assertAllowed(String login, String remoteAddress) {
        String key = buildKey(login, remoteAddress);
        AttemptState state = attempts.get(key);
        if (state == null || state.lockedUntil == null) {
            return;
        }
        if (state.lockedUntil.isAfter(now())) {
            throw new LoginRateLimitException("Muitas tentativas de login. Tente novamente mais tarde.");
        }
        attempts.remove(key, state);
    }

    public void recordSuccess(String login, String remoteAddress) {
        attempts.remove(buildKey(login, remoteAddress));
    }

    public void recordFailure(String login, String remoteAddress) {
        attempts.compute(buildKey(login, remoteAddress), (key, currentState) -> {
            AttemptState state = currentState == null ? new AttemptState(0, null) : currentState;
            if (state.lockedUntil != null && state.lockedUntil.isAfter(now())) {
                return state;
            }

            int failures = state.failures + 1;
            Instant lockedUntil = failures >= maxFailures ? now().plus(lockDuration) : null;
            return new AttemptState(failures, lockedUntil);
        });
    }

    private String buildKey(String login, String remoteAddress) {
        String normalizedLogin = normalize(login, UNKNOWN_LOGIN);
        String normalizedRemoteAddress = normalize(remoteAddress, UNKNOWN_IP);
        return normalizedLogin + "|" + normalizedRemoteAddress;
    }

    private String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private record AttemptState(int failures, Instant lockedUntil) {
    }
}
