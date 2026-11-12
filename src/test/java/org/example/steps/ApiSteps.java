package org.example.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.example.framework.api.ApiClient;

import static org.testng.Assert.assertEquals;

public class ApiSteps {
    private final ApiClient client = new ApiClient();
    private Response response;

    @When("I request post {int}")
    public void requestPost(int id) {
        response = client.getPost(id);
    }

    @Then("the response status should be {int}")
    public void verifyStatus(int status) {
        assertEquals(response.statusCode(), status);
    }

    @Then("the response should contain title {string}")
    public void verifyTitle(String title) {
        assertEquals(response.jsonPath().getString("title"), title);
    }
}
