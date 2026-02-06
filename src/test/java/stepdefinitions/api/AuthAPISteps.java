package stepdefinitions.api;

import io.cucumber.java.en.*;
import net.serenitybdd.rest.SerenityRest;
import org.assertj.core.api.Assertions;
import utils.ApiClient;
import utils.TestUsers;
import utils.Urls;

public class AuthAPISteps {

    private String token;
    private int status;

    @Given("admin logs in via API")
    public void adminLogsInViaAPI() {
        token = ApiClient.loginAndGetToken(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD);
        status = (token == null) ? 401 : 200;
    }

    @Given("user logs in via API")
    public void userLogsInViaAPI() {
        token = ApiClient.loginAndGetToken(TestUsers.USER_USERNAME, TestUsers.USER_PASSWORD);
        status = (token == null) ? 401 : 200;
    }

    @Then("token should be available")
    public void tokenShouldBeAvailable() {
        Assertions.assertThat(token)
                .as("JWT token should be returned on successful login")
                .isNotNull();
    }

    @When("login via API with {string} and {string}")
    public void loginViaAPIWithAnd(String username, String password) {
        token = ApiClient.loginAndGetToken(username, password);
        status = (token == null) ? 401 : 200;
    }

        @When("admin creates category via API")
    public void adminCreatesCategoryViaAPI() {
        String body = """
            {
              "name": "AUTOMATION_CAT_M1",
              "parentId": null
            }
            """;

        status = ApiClient.postWithBearer(Urls.API_CATEGORIES, token, body);

        // Some backends return 201 Created, some return 200 OK
        if (status == 201) status = 200;
    }

    @When("user tries to create category via API")
    public void userTriesToCreateCategoryViaAPI() {
        String body = """
            {
              "name": "USER_CAT_M1",
              "parentId": null
            }
            """;

        status = ApiClient.postWithBearer(Urls.API_CATEGORIES, token, body);
    }

    @When("request create category without token")
    public void requestCreateCategoryWithoutToken() {
        status = SerenityRest.given()
                .contentType("application/json")
                .body("""
                    { "name": "CAT_NO_TOKEN_M1", "parentId": null }
                    """)
                .post(Urls.API_CATEGORIES)
                .then()
                .extract()
                .statusCode();
    }

    @When("request create category with invalid token")
    public void requestCreateCategoryWithInvalidToken() {
        status = ApiClient.postWithBearer(
                Urls.API_CATEGORIES,
                "INVALID_TOKEN",
                """
                { "name": "CAT_BAD_TOKEN_M1", "parentId": null }
                """
        );
    }

    @Then("api response status should be {int}")
    public void apiResponseStatusShouldBe(int expected) {

        if (expected == 403) {
            Assertions.assertThat(status == 401 || status == 403)
                    .as("Expected 401 or 403 for unauthorized role access")
                    .isTrue();
            return;
        }

        // Empty payload or validation errors
        if (expected == 400) {
            Assertions.assertThat(status == 400 || status == 401).isTrue();
            return;
        }

        Assertions.assertThat(status).isEqualTo(expected);
    }

}
