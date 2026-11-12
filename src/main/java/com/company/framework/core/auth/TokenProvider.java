package com.company.framework.core.auth;

import com.company.framework.core.config.ConfigManager;

import java.time.Instant;

public final class TokenProvider {
    private static volatile Token token;

    private TokenProvider() {}

    public static String getToken() {
        Token current = token;
        if (current == null || current.expiresAt().isBefore(Instant.now())) {
            synchronized (TokenProvider.class) {
                current = token;
                if (current == null || current.expiresAt().isBefore(Instant.now())) {
                    String configured = ConfigManager.optional("api.token");
                    token = current = new Token(configured == null ? "" : configured,
                            Instant.now().plusSeconds(ConfigManager.integer("api.token.ttl.seconds")));
                }
            }
        }
        return current.value();
    }

    private record Token(String value, Instant expiresAt) {}
}
