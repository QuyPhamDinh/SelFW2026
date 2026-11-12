package org.example.framework.api;

import io.restassured.response.Response;
import com.company.framework.api.ApiSpecs;
import static io.restassured.RestAssured.given;

public final class ApiClient {
    public Response getPost(int id) {
        return given()
                .spec(ApiSpecs.base())
                .when()
                .get("/posts/{id}", id);
    }
}
