package com.company.framework.api.clients;

import com.company.framework.api.ApiSpecs;
import com.company.framework.api.models.Post;

import static io.restassured.RestAssured.given;

public final class PostsClient {
    public Post get(int id) {
        return given()
                .spec(ApiSpecs.base())
                .when()
                .get("/posts/{id}", id)
                .then()
                .statusCode(200)
                .extract()
                .as(Post.class);
    }
}
