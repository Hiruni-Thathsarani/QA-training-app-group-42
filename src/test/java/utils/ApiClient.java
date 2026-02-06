package utils;

import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;

public class ApiClient {

    public static String loginAndGetToken(String username, String password) {

        var response = SerenityRest.given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(username, password))
                .post(Urls.API_LOGIN)
                .then()
                .extract()
                .response();

        if (response.statusCode() != 200) {
            return null;
        }

        String token = response.jsonPath().getString("token");
        if (token == null || token.isBlank()) {
            token = response.jsonPath().getString("accessToken");
        }

        return token;
    }

    public static int postWithBearer(String url, String token, String jsonBody) {
        return SerenityRest.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(jsonBody)
                .post(url)
                .then()
                .extract()
                .statusCode();
    }

    public static int getWithBearer(String url, String token) {
        return SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .get(url)
                .then()
                .extract()
                .statusCode();
    }

    public static io.restassured.response.Response getResponseWithBearer(String url, String token) {
        return SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .get(url)
                .then()
                .extract()
                .response();
    }

    public static int putWithBearer(String url, String token, String jsonBody) {
        return SerenityRest.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(jsonBody)
                .put(url)
                .then()
                .extract()
                .statusCode();
    }

    public static int deleteWithBearer(String url, String token) {
        return SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .delete(url)
                .then()
                .extract()
                .statusCode();
    }

    public static io.restassured.response.Response deleteResponseWithBearer(String url, String token) {
        return SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .delete(url)
                .then()
                .extract()
                .response();
    }

    public static io.restassured.response.Response postResponseWithBearer(String url, String token, String jsonBody) {
        return SerenityRest.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(jsonBody)
                .post(url)
                .then()
                .extract()
                .response();
    }
}
