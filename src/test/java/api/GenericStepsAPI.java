package api;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.AfterAll;
import io.cucumber.java.en.*;
import io.restassured.response.Response;
import utils.ApiUtils;
import utils.MockServerUtils;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

public class GenericStepsAPI {
    private Response response;

    @BeforeAll
    public static void setup() {
        System.out.println("Initializing API Test Setup...");
        MockServerUtils.startMockServer();
    }

    @AfterAll
    public static void teardown() {
        MockServerUtils.stopMockServer();
        System.out.println("API Test Execution Completed.");
    }

    @When("^I send a GET request to \"([^\"]*)\"$")
    public void sendGetRequest(String endpoint) {
        response = ApiUtils.get(endpoint);
    }

    @When("^I send a POST request to \"([^\"]*)\" with payload:$")
    public void sendPostRequest(String endpoint, Map<String, String> payload) {
        response = ApiUtils.post(endpoint, payload);
    }

    @When("^I send a PUT request to \"([^\"]*)\" with payload:$")
    public void sendPutRequest(String endpoint, Map<String, String> payload) {
        response = ApiUtils.put(endpoint, payload);
    }

    @When("^I send a DELETE request to \"([^\"]*)\"$")
    public void sendDeleteRequest(String endpoint) {
        response = ApiUtils.delete(endpoint);
    }

    @Then("^the response status code should be (\\d+)$")
    public void verifyStatusCode(int expectedStatusCode) {
        assertEquals(response.getStatusCode(), expectedStatusCode, "Unexpected status code");
    }

    @Then("^the response body should contain \"([^\"]*)\" with value \"([^\"]*)\"$")
    public void verifyResponseBody(String key, String expectedValue) {
        response.then().body(key, equalTo(expectedValue));
    }

    @Then("^the response body should match:$")
    public void verifyResponseBodyMatch(Map<String, String> expectedValues) {
        for (Map.Entry<String, String> entry : expectedValues.entrySet()) {
            response.then().body(entry.getKey(), equalTo(entry.getValue()));
        }
    }

    @Then("^the response body should be empty$")
    public void the_response_body_should_be_empty() {
        String responseBody = response.getBody().asString().trim();
        assertEquals(responseBody, "", "Expected response body to be empty, but it was not.");
    }

    @Then("^the response body should contain a count of (\\d+)$")
    public void verifyResponseBodyCount(int expectedCount) {
        int actualCount = response.then().extract().path("count");
        assertEquals(actualCount, expectedCount, "The 'count' field does not match the expected value.");
    }

    @Then("^the response body should have (\\d+) people in results$")
    public void verifyResultsSize(int expectedSize) {
        int actualSize = response.then().extract().path("results.size()");
        assertEquals(actualSize, expectedSize, "The 'results' array size does not match the expected value.");
    }

}
