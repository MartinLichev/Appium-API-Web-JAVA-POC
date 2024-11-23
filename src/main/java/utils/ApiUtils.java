package utils;


import io.restassured.response.Response;

import static io.restassured.RestAssured.*;

public class ApiUtils {

    // GET Request
    public static Response get(String url) {
        return given()
                .when()
                .get(url)
                .then()
                .extract()
                .response();
    }

    // POST Request
    public static Response post(String url, Object payload) {
        return given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post(url)
                .then()
                .extract()
                .response();
    }

    // PUT Request
    public static Response put(String url, Object payload) {
        return given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put(url)
                .then()
                .extract()
                .response();
    }

    // DELETE Request
    public static Response delete(String url) {
        return given()
                .when()
                .delete(url)
                .then()
                .extract()
                .response();
    }
}
