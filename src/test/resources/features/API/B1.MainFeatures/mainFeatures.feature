Feature: Star Wars API Tests

    Scenario: Get details of a person
        When I send a GET request to "/people/1"
        Then the response status code should be 200
        And the response body should contain "name" with value "Luke Skywalker"

    Scenario: Get details of a starship
        When I send a GET request to "/starships/9"
        Then the response status code should be 200
        And the response body should contain "name" with value "Death Star"

    Scenario: Add a new person (Mock POST)
        When I send a POST request to "/people" with payload:
            | name       | Obi-Wan Kenobi |
            | gender     | male           |
            | birth_year | 57BBY          |
        Then the response status code should be 201
        And the response body should contain "name" with value "Obi-Wan Kenobi"

    Scenario: Update an existing person (Mock PUT)
        When I send a PUT request to "/people/1" with payload:
            | name       | Luke Skywalker |
            | gender     | male           |
            | birth_year | 19BBY          |
        Then the response status code should be 200
        And the response body should contain "name" with value "Luke Skywalker"

    Scenario: Delete a person (Mock DELETE)
        When I send a DELETE request to "/people/1"
        Then the response status code should be 204
        And the response body should be empty
