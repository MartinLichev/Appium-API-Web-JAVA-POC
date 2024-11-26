package utils;

import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

public class MockServerUtils {
        private static ClientAndServer mockServer;

        public static void startMockServer() {
                if (mockServer == null) {
                        mockServer = ClientAndServer.startClientAndServer(1080);

                        // Mock GET request for "/people"
                        mockServer.when(
                                        HttpRequest.request()
                                                        .withMethod("GET")
                                                        .withPath("/people"))
                                        .respond(
                                                        HttpResponse.response()
                                                                        .withStatusCode(200)
                                                                        .withHeader("Content-Type", "application/json")
                                                                        .withBody("{\n" +
                                                                                        "  \"count\": 3,\n" +
                                                                                        "  \"results\": [\n" +
                                                                                        "    {\n" +
                                                                                        "      \"name\": \"Luke Skywalker\",\n"
                                                                                        +
                                                                                        "      \"gender\": \"male\",\n"
                                                                                        +
                                                                                        "      \"birth_year\": \"19BBY\"\n"
                                                                                        +
                                                                                        "    },\n" +
                                                                                        "    {\n" +
                                                                                        "      \"name\": \"Leia Organa\",\n"
                                                                                        +
                                                                                        "      \"gender\": \"female\",\n"
                                                                                        +
                                                                                        "      \"birth_year\": \"19BBY\"\n"
                                                                                        +
                                                                                        "    },\n" +
                                                                                        "    {\n" +
                                                                                        "      \"name\": \"Han Solo\",\n"
                                                                                        +
                                                                                        "      \"gender\": \"male\",\n"
                                                                                        +
                                                                                        "      \"birth_year\": \"29BBY\"\n"
                                                                                        +
                                                                                        "    }\n" +
                                                                                        "  ]\n" +
                                                                                        "}"));

                        // Mock GET request for "/people/1"
                        mockServer.when(
                                        HttpRequest.request()
                                                        .withMethod("GET")
                                                        .withPath("/people/1"))
                                        .respond(
                                                        HttpResponse.response()
                                                                        .withStatusCode(200)
                                                                        .withHeader("Content-Type", "application/json")
                                                                        .withBody("{\"name\": \"Luke Skywalker\", \"gender\": \"male\", \"birth_year\": \"19BBY\"}"));

                        // Mock GET request for "/starships/9"
                        mockServer.when(
                                        HttpRequest.request()
                                                        .withMethod("GET")
                                                        .withPath("/starships/9"))
                                        .respond(
                                                        HttpResponse.response()
                                                                        .withStatusCode(200)
                                                                        .withHeader("Content-Type", "application/json")
                                                                        .withBody("{\"name\": \"Death Star\"}"));

                        // Mock POST request for "/people"
                        mockServer.when(
                                        HttpRequest.request()
                                                        .withMethod("POST")
                                                        .withPath("/people"))
                                        .respond(
                                                        HttpResponse.response()
                                                                        .withStatusCode(201)
                                                                        .withHeader("Content-Type", "application/json")
                                                                        .withBody("{\"id\": 101, \"name\": \"Obi-Wan Kenobi\"}"));

                        // Mock PUT request for "/people/1"
                        mockServer.when(
                                        HttpRequest.request()
                                                        .withMethod("PUT")
                                                        .withPath("/people/1"))
                                        .respond(
                                                        HttpResponse.response()
                                                                        .withStatusCode(200)
                                                                        .withHeader("Content-Type", "application/json")
                                                                        .withBody("{\"name\": \"Luke Skywalker\", \"gender\": \"male\", \"birth_year\": \"19BBY\"}"));

                        // Mock DELETE request for "/people/1"
                        mockServer.when(
                                        HttpRequest.request()
                                                        .withMethod("DELETE")
                                                        .withPath("/people/1"))
                                        .respond(
                                                        HttpResponse.response()
                                                                        .withStatusCode(204));

                        System.out.println("Mock server started at: http://localhost:1080");
                }
        }

        public static void stopMockServer() {
                if (mockServer != null) {
                        mockServer.stop();
                        System.out.println("Mock server stopped.");
                }
        }
}
