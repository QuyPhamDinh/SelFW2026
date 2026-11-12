@api
Feature: Posts API

  @smoke
  Scenario: Retrieve a post
    When I request post 1
    Then the response status should be 200
    And the response should contain title "sunt aut facere repellat provident occaecati excepturi optio reprehenderit"
