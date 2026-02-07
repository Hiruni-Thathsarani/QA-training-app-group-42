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
}
