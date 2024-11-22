package api;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.AfterAll;
import io.cucumber.java.en.*;
import io.restassured.response.Response;
import utils.ApiUtils;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

public class GenericStepsAPI {
    private Response response;
    private String baseUrl;

    @BeforeAll
    public static void setup() {
        System.out.println("Initializing API Test Setup...");
        // Load environment variables or configurations
    }

    @AfterAll
    public static void teardown() {
        System.out.println("API Test Execution Completed.");
    }

    @Given("^I set the API base URL to \"([^\"]*)\"$")
    public void setBaseUrl(String url) {
        this.baseUrl = url;
    }

    @When("^I send a GET request to \"([^\"]*)\"$")
    public void sendGetRequest(String endpoint) {
        String url = constructUrl(endpoint);
        response = ApiUtils.get(url);
    }

    @When("^I send a POST request to \"([^\"]*)\" with payload:$")
    public void sendPostRequest(String endpoint, Map<String, String> payload) {
        String url = constructUrl(endpoint);
        response = ApiUtils.post(url, payload);
    }

    @When("^I send a PUT request to \"([^\"]*)\" with payload:$")
    public void sendPutRequest(String endpoint, Map<String, String> payload) {
        String url = constructUrl(endpoint);
        response = ApiUtils.put(url, payload);
    }

    @When("^I send a DELETE request to \"([^\"]*)\"$")
    public void sendDeleteRequest(String endpoint) {
        String url = constructUrl(endpoint);
        response = ApiUtils.delete(url);
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

    // Helper method to construct the complete URL
    private String constructUrl(String endpoint) {
        if (baseUrl == null) {
            throw new IllegalStateException("Base URL is not set. Please set the base URL using the step: I set the API base URL to \"<URL>\"");
        }
        return baseUrl + endpoint;
    }
}
