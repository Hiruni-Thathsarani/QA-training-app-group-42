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

    public static int getWithBearer(String url, String token) {
        return SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .get(url)
                .then()
                .extract()
                .statusCode();
    }
}
