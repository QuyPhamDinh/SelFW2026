package org.example.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.framework.web.WebFormPage;

import static org.testng.Assert.assertEquals;

public class UiSteps {
    private WebFormPage page;

    @Given("I open the Selenium web form")
    public void openForm() {
        page = new WebFormPage();
        page.open();
    }

    @When("I submit the form with message {string}")
    public void submitForm(String message) {
        page.submitMessage(message);
    }

    @Then("the form result should be {string}")
    public void verifyResult(String expected) {
        assertEquals(page.result(), expected);
    }
}
