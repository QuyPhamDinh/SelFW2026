package com.company.framework.core.data;

import java.util.UUID;

public final class UserFactory {
    private UserFactory() {}

    public static UserData build() {
        String id = UUID.randomUUID().toString().substring(0, 8);
        return new UserData("automation-" + id, "automation-" + id + "@example.test");
    }

    public record UserData(String username, String email) {}
}
