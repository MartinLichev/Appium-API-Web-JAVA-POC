package utils;

import io.github.cdimascio.dotenv.Dotenv;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

import java.util.Map;

public class ApiUtils {
  // Initialize Dotenv for loading environment variables from the `.env` file
  private static final Dotenv dotenv = Dotenv.configure().load();

  private static final String BASE_URL;

  static {
    // Load BASE_URL with priority: System Property > Environment Variable > .env
    BASE_URL = System.getProperty("API_BASE_URL",
        System.getenv("API_BASE_URL") != null ? System.getenv("API_BASE_URL") : dotenv.get("API_BASE_URL"));

    if (BASE_URL == null || BASE_URL.isEmpty()) {
      throw new IllegalStateException(
          "API_BASE_URL is not configured. Set it in the .env file, as a system property, or as an environment variable.");
    }

    System.out.println("Base URL initialized: " + BASE_URL);
  }

  /**
   * Send a GET request to the given endpoint
   */
  public static Response get(String endpoint) {
    return given()
        .baseUri(BASE_URL)
        .contentType("application/json")
        .when()
        .get(endpoint)
        .then()
        .extract()
        .response();
  }

  /**
   * Send a POST request to the given endpoint with the provided payload
   */
  public static Response post(String endpoint, Map<String, String> payload) {
    return given()
        .baseUri(BASE_URL)
        .contentType("application/json")
        .body(payload)
        .when()
        .post(endpoint)
        .then()
        .extract()
        .response();
  }

  /**
   * Send a PUT request to the given endpoint with the provided payload
   */
  public static Response put(String endpoint, Map<String, String> payload) {
    return given()
        .baseUri(BASE_URL)
        .contentType("application/json")
        .body(payload)
        .when()
        .put(endpoint)
        .then()
        .extract()
        .response();
  }

  /**
   * Send a DELETE request to the given endpoint
   */
  public static Response delete(String endpoint) {
    return given()
        .baseUri(BASE_URL)
        .contentType("application/json")
        .when()
        .delete(endpoint)
        .then()
        .extract()
        .response();
  }
}
