@ui
Feature: Selenium web form

  @smoke
  Scenario: Submit the web form
    Given I open the Selenium web form
    When I submit the form with message "Framework ready"
    Then the form result should be "Received!"
