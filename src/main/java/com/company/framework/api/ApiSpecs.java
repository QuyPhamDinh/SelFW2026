package com.company.framework.api;

import com.company.framework.core.auth.TokenProvider;
import com.company.framework.core.config.ConfigManager;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class ApiSpecs {
    private ApiSpecs() {}

    public static RequestSpecification base() {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(ConfigManager.get("api.base.url"))
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setConfig(RestAssuredConfig.config()
                        .httpClient(HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", 10_000)
                                .setParam("http.socket.timeout", 10_000)
                                .setParam("http.connection-manager.timeout", 10_000)));
        String token = TokenProvider.getToken();
        if (token != null && !token.isBlank()) {
            builder.addHeader("Authorization", "Bearer " + token);
        }
        return builder.build();
    }
}
