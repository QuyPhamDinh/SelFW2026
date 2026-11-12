package com.company.framework.api;

import com.company.framework.core.auth.TokenProvider;
import com.company.framework.core.config.ConfigManager;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class ApiSpecs {
    private ApiSpecs() {}

    public static RequestSpecification base() {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(ConfigManager.get("api.base.url"))
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON);
        String token = TokenProvider.getToken();
        if (token != null && !token.isBlank()) {
            builder.addHeader("Authorization", "Bearer " + token);
        }
        return builder.build();
    }
}
