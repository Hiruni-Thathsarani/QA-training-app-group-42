// ApiClient.java
package utils;

import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;

public class ApiClient {

    public static LoginResult login(String username, String password) {
        try {
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

            int status = response.statusCode();
            if (status != 200) {
                return new LoginResult(null, status);
            }

            String token = response.jsonPath().getString("token");
            if (token == null || token.isBlank()) token = response.jsonPath().getString("accessToken");
            if (token == null || token.isBlank()) token = response.jsonPath().getString("access_token");
            if (token == null || token.isBlank()) token = response.jsonPath().getString("jwt");
            if (token == null || token.isBlank()) token = response.jsonPath().getString("data.token");
            if (token == null || token.isBlank()) token = response.jsonPath().getString("data.accessToken");
            if (token == null || token.isBlank()) token = response.jsonPath().getString("data.access_token");

            return new LoginResult(token, status);
        } catch (Exception e) {
            return new LoginResult(null, 0);
        }
    }

    public static String loginAndGetToken(String username, String password) {
        return login(username, password).token();
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

    public static String createCategory(String token, String name, String parentId) {
        String body;
        if (parentId == null) {
            body = """
                { "name": "%s" }
                """.formatted(name);
        } else {
            body = """
                { "name": "%s", "parentId": "%s" }
                """.formatted(name, parentId);
        }

        var response = SerenityRest.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(body)
                .post(Urls.API_CATEGORIES)
                .then()
                .extract()
                .response();

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            return null;
        }

        String id = response.jsonPath().getString("id");
        if (id == null || id.isBlank()) id = response.jsonPath().getString("data.id");
        if (id == null || id.isBlank()) id = response.jsonPath().getString("categoryId");
        return id;
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
