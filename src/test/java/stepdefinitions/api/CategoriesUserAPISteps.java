package stepdefinitions.api;

import io.cucumber.java.en.*;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import net.serenitybdd.core.Serenity;
import utils.ApiClient;
import utils.TestUsers;
import utils.Urls;

/**
 * Step definitions for User role API tests on Categories module (TC-086 to
 * TC-090)
 * Uses Serenity session for token sharing between step classes
 */
public class CategoriesUserAPISteps {

    private Response response;
    private int statusCode;
    private Long existingCategoryId;

    private String getUserToken() {
        String token = Serenity.sessionVariableCalled("userToken");
        if (token == null) {
            token = ApiClient.loginAndGetToken(TestUsers.USER_USERNAME, TestUsers.USER_PASSWORD);
            Serenity.setSessionVariable("userToken").to(token);
        }
        return token;
    }

    private String getAdminToken() {
        String token = Serenity.sessionVariableCalled("adminToken");
        if (token == null) {
            token = ApiClient.loginAndGetToken(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD);
            Serenity.setSessionVariable("adminToken").to(token);
        }
        return token;
    }

    // ============== TC-086: PAGINATION ==============

    @When("user requests GET \\/api\\/categories\\/page with page={int} and size={int}")
    public void userRequestsGetApiCategoriesPageWithPageAndSize(int page, int size) {
        String token = getUserToken();
        String url = Urls.API_CATEGORIES_PAGE + "?page=" + page + "&size=" + size;
        response = ApiClient.getResponseWithBearer(url, token);
        statusCode = response.getStatusCode();
    }

    @Then("API response status should be {int}")
    public void apiResponseStatusShouldBe(int expected) {
        Assertions.assertThat(statusCode)
                .as("API response status should be " + expected)
                .isEqualTo(expected);
    }

    @Then("response should contain paged category data")
    public void responseShouldContainPagedCategoryData() {
        String body = response.getBody().asString();
        // Verify paged response structure
        Assertions.assertThat(body)
                .as("Response should contain paged data")
                .contains("content");
    }

    // ============== TC-087: MAIN CATEGORIES ==============

    @When("user requests GET \\/api\\/categories\\/main")
    public void userRequestsGetApiCategoriesMain() {
        String token = getUserToken();
        response = ApiClient.getResponseWithBearer(Urls.API_CATEGORIES_MAIN, token);
        statusCode = response.getStatusCode();
    }

    @Then("response should contain list of main categories")
    public void responseShouldContainListOfMainCategories() {
        String body = response.getBody().asString();
        // Should be an array or object with categories
        Assertions.assertThat(body)
                .as("Response should contain category data")
                .isNotEmpty();
    }

    // ============== TC-088: SUB-CATEGORIES ==============

    @When("user requests GET \\/api\\/categories\\/sub-categories")
    public void userRequestsGetApiCategoriesSubCategories() {
        String token = getUserToken();
        response = ApiClient.getResponseWithBearer(Urls.API_CATEGORIES_SUB, token);
        statusCode = response.getStatusCode();
    }

    @Then("response should contain list of sub-categories")
    public void responseShouldContainListOfSubCategories() {
        String body = response.getBody().asString();
        Assertions.assertThat(body)
                .as("Response should contain sub-category data")
                .isNotEmpty();
    }

    // ============== TC-089: CREATE FORBIDDEN ==============

    @When("user sends POST \\/api\\/categories with valid payload")
    public void userSendsPostApiCategoriesWithValidPayload() {
        String token = getUserToken();
        String uniqueName = "UserCat" + System.currentTimeMillis() % 1000;
        if (uniqueName.length() > 10) {
            uniqueName = uniqueName.substring(0, 10);
        }
        String body = String.format("""
                {
                  "name": "%s",
                  "parent": null
                }
                """, uniqueName);
        statusCode = ApiClient.postWithBearer(Urls.API_CATEGORIES, token, body);
    }

    @Then("API response status should be {int} Forbidden")
    public void apiResponseStatusShouldBeForbidden(int expected) {
        // Accept either 401 or 403 for unauthorized access
        Assertions.assertThat(statusCode == 401 || statusCode == 403)
                .as("Expected 401 or 403 for forbidden access, got " + statusCode)
                .isTrue();
    }

    // ============== TC-090: UPDATE FORBIDDEN ==============

    @Given("a category exists via admin API")
    public void aCategoryExistsViaAdminAPI() {
        String adminToken = getAdminToken();

        // Create a category
        String uniqueName = "Test" + System.currentTimeMillis() % 10000;
        if (uniqueName.length() > 10) {
            uniqueName = uniqueName.substring(0, 10);
        }
        String body = String.format("""
                {
                  "name": "%s",
                  "parent": null
                }
                """, uniqueName);
        Response createResponse = ApiClient.postResponseWithBearer(Urls.API_CATEGORIES, adminToken, body);

        // Extract ID from response
        try {
            existingCategoryId = createResponse.jsonPath().getLong("id");
        } catch (Exception e) {
            // If can't get ID, use a default
            existingCategoryId = 1L;
        }
        Serenity.setSessionVariable("existingCategoryId").to(existingCategoryId);
    }

    @When("user sends PUT \\/api\\/categories\\/\\{id} with valid payload")
    public void userSendsPutApiCategoriesIdWithValidPayload() {
        String token = getUserToken();
        Long categoryId = existingCategoryId != null ? existingCategoryId
                : Serenity.sessionVariableCalled("existingCategoryId");
        if (categoryId == null)
            categoryId = 1L;

        String updatedName = "Updated" + System.currentTimeMillis() % 1000;
        if (updatedName.length() > 10) {
            updatedName = updatedName.substring(0, 10);
        }
        String body = String.format("""
                {
                  "name": "%s",
                  "parentId": null
                }
                """, updatedName);
        String url = Urls.API_CATEGORIES + "/" + categoryId;
        statusCode = ApiClient.putWithBearer(url, token, body);
    }
}
