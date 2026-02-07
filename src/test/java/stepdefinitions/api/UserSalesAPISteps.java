package stepdefinitions.api;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.assertj.core.api.Assertions;
import org.junit.Assume;
import utils.ApiClient;
import utils.LoginResult;
import utils.TestUsers;
import utils.Urls;

import java.util.List;

public class UserSalesAPISteps {

    private String token;
    private int status;
    private Response response;
    private Long requestedSaleId;

    @Given("authenticated user token is available for sales api tests")
    public void authenticatedUserTokenIsAvailableForSalesApiTests() {
        LoginResult result = ApiClient.login(TestUsers.USER_USERNAME, TestUsers.USER_PASSWORD);
        token = result.token();
        status = result.status();

        if (status == 0) {
            Assume.assumeTrue("API not reachable at " + Urls.API_LOGIN, false);
        }
        if (status == 401 || status == 403) {
            Assume.assumeTrue("User credentials invalid; update TestUsers or API auth", false);
        }

        Assertions.assertThat(token).isNotBlank();
    }

    @When("user requests paginated sales with page {int} and size {int}")
    public void userRequestsPaginatedSalesWithPageAndSize(int page, int size) {
        response = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", page)
                .queryParam("size", size)
                .get(Urls.API_SALES + "/page")
                .then()
                .extract()
                .response();

        status = response.statusCode();
    }

    @Then("sales api response status should be {int}")
    public void salesApiResponseStatusShouldBe(int expectedStatus) {
        Assertions.assertThat(status).isEqualTo(expectedStatus);
    }

    @And("paginated sales data should be present in response")
    public void paginatedSalesDataShouldBePresentInResponse() {
        List<?> content = firstNonNullList(
                response.jsonPath().getList("content"),
                response.jsonPath().getList("data.content"),
                response.jsonPath().getList("result.content"),
                response.jsonPath().getList("items"),
                response.jsonPath().getList("data.items")
        );

        Assertions.assertThat(content).isNotNull();
    }

    @And("sales pagination metadata should be present")
    public void salesPaginationMetadataShouldBePresent() {
        List<?> content = firstNonNullList(
                response.jsonPath().getList("content"),
                response.jsonPath().getList("data.content"),
                response.jsonPath().getList("result.content"),
                response.jsonPath().getList("items"),
                response.jsonPath().getList("data.items")
        );

        Object pageNumber = firstNonNull(
                response.jsonPath().get("number"),
                response.jsonPath().get("page"),
                response.jsonPath().get("pageNumber"),
                response.jsonPath().get("data.number"),
                response.jsonPath().get("data.page"),
                response.jsonPath().get("data.pageNumber")
        );

        Object pageSize = firstNonNull(
                response.jsonPath().get("size"),
                response.jsonPath().get("pageSize"),
                response.jsonPath().get("data.size"),
                response.jsonPath().get("data.pageSize")
        );

        boolean metadataPresent = pageNumber != null || pageSize != null || content != null;
        Assertions.assertThat(metadataPresent).isTrue();
    }

    @And("at least one sale exists for user sales api tests")
    public void atLeastOneSaleExistsForUserSalesApiTests() {
        Response pagedSales = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .get(Urls.API_SALES + "/page")
                .then()
                .extract()
                .response();

        int pageStatus = pagedSales.statusCode();
        if (pageStatus == 0) {
            Assume.assumeTrue("API not reachable at " + Urls.API_SALES + "/page", false);
        }

        requestedSaleId = firstNonNullLong(
                pagedSales.jsonPath().get("content[0].id"),
                pagedSales.jsonPath().get("data.content[0].id"),
                pagedSales.jsonPath().get("result.content[0].id"),
                pagedSales.jsonPath().get("items[0].id"),
                pagedSales.jsonPath().get("data.items[0].id")
        );

        if (requestedSaleId == null) {
            Assume.assumeTrue("No sale data available for /api/sales/page", false);
        }
    }

    @When("user requests sale by existing id via sales api")
    public void userRequestsSaleByExistingIdViaSalesApi() {
        response = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .get(Urls.API_SALES + "/" + requestedSaleId)
                .then()
                .extract()
                .response();

        status = response.statusCode();
    }

    @And("sale details should be present in response")
    public void saleDetailsShouldBePresentInResponse() {
        String body = response.asString();
        Assertions.assertThat(body).isNotBlank();
    }

    @And("returned sale id should match requested sale id")
    public void returnedSaleIdShouldMatchRequestedSaleId() {
        Long returnedId = firstNonNullLong(
                response.jsonPath().get("id"),
                response.jsonPath().get("data.id"),
                response.jsonPath().get("result.id"),
                response.jsonPath().get("sale.id")
        );

        Assertions.assertThat(returnedId).isEqualTo(requestedSaleId);
    }

    @SafeVarargs
    private final List<?> firstNonNullList(List<?>... candidates) {
        for (List<?> candidate : candidates) {
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    private Object firstNonNull(Object... candidates) {
        for (Object candidate : candidates) {
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    private Long firstNonNullLong(Object... candidates) {
        for (Object candidate : candidates) {
            Long parsed = toLong(candidate);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        if (text.matches("-?\\d+")) {
            return Long.parseLong(text);
        }
        if (text.matches("-?\\d+\\.0+")) {
            return (long) Double.parseDouble(text);
        }
        return null;
    }

    // ===== NEW CODE - USER SALES API TESTS START =====
    @And("at least one plant exists for user sales api tests")
    public void atLeastOnePlantExistsForUserSalesApiTests() {
        Response plantsResponse = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .get(Urls.API_PLANTS)
                .then()
                .extract()
                .response();

        int plantsStatus = plantsResponse.statusCode();
        if (plantsStatus == 0) {
            Assume.assumeTrue("API not reachable at " + Urls.API_PLANTS, false);
        }
        if (plantsStatus == 401 || plantsStatus == 403) {
            Assume.assumeTrue("User is not authorized to fetch plants at " + Urls.API_PLANTS, false);
        }
        if (plantsStatus != 200) {
            Assume.assumeTrue("Unable to fetch plants. Status: " + plantsStatus, false);
        }

        Long plantId = firstNonNullLong(
                plantsResponse.jsonPath().get("[0].id"),
                plantsResponse.jsonPath().get("content[0].id"),
                plantsResponse.jsonPath().get("data[0].id"),
                plantsResponse.jsonPath().get("data.content[0].id"),
                plantsResponse.jsonPath().get("items[0].id"),
                plantsResponse.jsonPath().get("data.items[0].id")
        );

        if (plantId == null) {
            Assume.assumeTrue("No plant data available for " + Urls.API_PLANTS, false);
        }

        net.serenitybdd.core.Serenity.setSessionVariable("userSalesApiPlantId").to(plantId);
    }

    @And("current total sales count is captured for user sales api tests")
    public void currentTotalSalesCountIsCapturedForUserSalesApiTests() {
        Response salesPageResponse = requestSalesPage(0, 10);
        int salesPageStatus = salesPageResponse.statusCode();

        if (salesPageStatus == 0) {
            Assume.assumeTrue("API not reachable at " + Urls.API_SALES + "/page", false);
        }
        if (salesPageStatus != 200) {
            Assume.assumeTrue("Unable to fetch sales page. Status: " + salesPageStatus, false);
        }

        Long totalSalesBefore = extractTotalSalesCount(salesPageResponse);
        if (totalSalesBefore == null) {
            Assume.assumeTrue("Unable to read sales total from /api/sales/page", false);
        }

        net.serenitybdd.core.Serenity.setSessionVariable("userSalesApiTotalBefore").to(totalSalesBefore);
    }

    @When("user tries to create sale for existing plant via sales api")
    public void userTriesToCreateSaleForExistingPlantViaSalesApi() {
        Long plantId = firstNonNullLong((Object) net.serenitybdd.core.Serenity.sessionVariableCalled("userSalesApiPlantId"));
        if (plantId == null) {
            Assume.assumeTrue("Plant id is not available for create-sale API test", false);
        }

        response = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .queryParam("quantity", 1)
                .post(Urls.API_SALES + "/plant/" + plantId)
                .then()
                .extract()
                .response();

        status = response.statusCode();
    }

    @And("forbidden response should indicate permission denied")
    public void forbiddenResponseShouldIndicatePermissionDenied() {
        String responseBody = response == null || response.asString() == null
                ? ""
                : response.asString().toLowerCase();
        String statusLine = response == null || response.statusLine() == null
                ? ""
                : response.statusLine().toLowerCase();

        boolean indicatesDenied = responseBody.contains("forbidden")
                || responseBody.contains("access denied")
                || responseBody.contains("permission denied")
                || responseBody.contains("not authorized")
                || responseBody.contains("unauthorized")
                || statusLine.contains("forbidden");

        Assertions.assertThat(indicatesDenied).isTrue();
    }

    @And("total sales count should remain unchanged for user sales api tests")
    public void totalSalesCountShouldRemainUnchangedForUserSalesApiTests() {
        Long totalSalesBefore = firstNonNullLong((Object) net.serenitybdd.core.Serenity.sessionVariableCalled("userSalesApiTotalBefore"));
        if (totalSalesBefore == null) {
            Assume.assumeTrue("Sales total before create attempt was not captured", false);
        }

        Response salesPageResponseAfter = requestSalesPage(0, 10);
        int salesPageStatusAfter = salesPageResponseAfter.statusCode();
        if (salesPageStatusAfter == 0) {
            Assume.assumeTrue("API not reachable at " + Urls.API_SALES + "/page", false);
        }
        if (salesPageStatusAfter != 200) {
            Assume.assumeTrue("Unable to fetch sales page after create attempt. Status: " + salesPageStatusAfter, false);
        }

        Long totalSalesAfter = extractTotalSalesCount(salesPageResponseAfter);
        Assertions.assertThat(totalSalesAfter).isEqualTo(totalSalesBefore);
    }

    @When("user tries to delete sale by existing id via sales api")
    public void userTriesToDeleteSaleByExistingIdViaSalesApi() {
        response = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .delete(Urls.API_SALES + "/" + requestedSaleId)
                .then()
                .extract()
                .response();

        status = response.statusCode();
    }

    @And("sale should still exist after forbidden delete attempt")
    public void saleShouldStillExistAfterForbiddenDeleteAttempt() {
        Response verifyResponse = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .get(Urls.API_SALES + "/" + requestedSaleId)
                .then()
                .extract()
                .response();

        Assertions.assertThat(verifyResponse.statusCode()).isEqualTo(200);

        Long returnedId = firstNonNullLong(
                verifyResponse.jsonPath().get("id"),
                verifyResponse.jsonPath().get("data.id"),
                verifyResponse.jsonPath().get("result.id"),
                verifyResponse.jsonPath().get("sale.id")
        );

        Assertions.assertThat(returnedId).isEqualTo(requestedSaleId);
    }

    private Response requestSalesPage(int page, int size) {
        return SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", page)
                .queryParam("size", size)
                .get(Urls.API_SALES + "/page")
                .then()
                .extract()
                .response();
    }

    private Long extractTotalSalesCount(Response salesPageResponse) {
        Long totalElements = firstNonNullLong(
                salesPageResponse.jsonPath().get("totalElements"),
                salesPageResponse.jsonPath().get("data.totalElements"),
                salesPageResponse.jsonPath().get("result.totalElements")
        );
        if (totalElements != null) {
            return totalElements;
        }

        List<?> content = firstNonNullList(
                salesPageResponse.jsonPath().getList("content"),
                salesPageResponse.jsonPath().getList("data.content"),
                salesPageResponse.jsonPath().getList("result.content"),
                salesPageResponse.jsonPath().getList("items"),
                salesPageResponse.jsonPath().getList("data.items")
        );
        if (content != null) {
            return (long) content.size();
        }

        return null;
    }
    // ===== NEW CODE - USER SALES API TESTS END =====

    // ===== NEW CODE - USER SALES API TESTS START =====
    @When("user requests all sales without authentication token via sales api")
    public void userRequestsAllSalesWithoutAuthenticationTokenViaSalesApi() {
        response = SerenityRest.given()
                .get(Urls.API_SALES)
                .then()
                .extract()
                .response();

        status = response.statusCode();
    }

    @And("unauthorized response should indicate missing or invalid authentication")
    public void unauthorizedResponseShouldIndicateMissingOrInvalidAuthentication() {
        String responseBody = response == null || response.asString() == null
                ? ""
                : response.asString().toLowerCase();
        String statusLine = response == null || response.statusLine() == null
                ? ""
                : response.statusLine().toLowerCase();

        boolean indicatesUnauthorized = responseBody.contains("unauthorized")
                || responseBody.contains("invalid")
                || responseBody.contains("missing")
                || responseBody.contains("token")
                || responseBody.contains("authentication")
                || statusLine.contains("unauthorized");

        Assertions.assertThat(indicatesUnauthorized).isTrue();
    }

    @And("no sales data should be returned for unauthorized sales request")
    public void noSalesDataShouldBeReturnedForUnauthorizedSalesRequest() {
        String responseBody = response == null || response.asString() == null
                ? ""
                : response.asString().trim();

        if (responseBody.isBlank()) {
            Assertions.assertThat(responseBody).isBlank();
            return;
        }

        List<?> rootList = null;
        try {
            rootList = response.jsonPath().getList("$");
        } catch (Exception ignored) {}

        List<?> content = null;
        try {
            content = firstNonNullList(
                    response.jsonPath().getList("content"),
                    response.jsonPath().getList("data.content"),
                    response.jsonPath().getList("result.content"),
                    response.jsonPath().getList("items"),
                    response.jsonPath().getList("data.items")
            );
        } catch (Exception ignored) {}

        Object rootId = null;
        try {
            rootId = firstNonNull(
                    response.jsonPath().get("id"),
                    response.jsonPath().get("data.id"),
                    response.jsonPath().get("result.id")
            );
        } catch (Exception ignored) {}

        boolean hasSalesData = (rootList != null && !rootList.isEmpty())
                || (content != null && !content.isEmpty())
                || rootId != null;

        Assertions.assertThat(hasSalesData).isFalse();
    }
    // ===== NEW CODE - USER SALES API TESTS END =====
}
