Feature: API Mock Setup

    Scenario: Verify API Mock is up and running
        When I send a GET request to "/people"
        Then the response status code should be 200
        And the response body should contain a count of 3
        And the response body should have 3 people in results