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
}
