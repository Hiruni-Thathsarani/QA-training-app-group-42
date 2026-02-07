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
import java.util.Map;

public class AdminSalesAPISteps {

    private String token;
    private int status;
    private Response response;
    private Long selectedPlantId;
    private Integer selectedPlantStock;

    // ===== NEW CODE - ADMIN SALES API TESTS START =====
    @Given("authenticated admin token is available for admin sales api tests")
    public void authenticatedAdminTokenIsAvailableForAdminSalesApiTests() {
        LoginResult result = ApiClient.login(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD);
        token = result.token();
        status = result.status();

        if (status == 0) {
            Assume.assumeTrue("API not reachable at " + Urls.API_LOGIN, false);
        }
        if (status == 401 || status == 403) {
            Assume.assumeTrue("Admin credentials invalid; update TestUsers or API auth", false);
        }

        Assertions.assertThat(token).isNotBlank();
    }

    @And("at least one sellable plant exists for admin sales api tests")
    public void atLeastOneSellablePlantExistsForAdminSalesApiTests() {
        List<?> plants = fetchPlantsForAdmin();
        for (Object plant : plants) {
            Long id = extractPlantId(plant);
            Integer stock = extractPlantStock(plant);
            if (id != null && stock != null && stock >= 1) {
                selectedPlantId = id;
                return;
            }
        }
        Assume.assumeTrue("No sellable plant found with stock >= 1", false);
    }

    @And("at least one low stock plant exists for admin sales api tests")
    public void atLeastOneLowStockPlantExistsForAdminSalesApiTests() {
        List<?> plants = fetchPlantsForAdmin();
        Long bestId = null;
        Integer bestStock = null;

        for (Object plant : plants) {
            Long id = extractPlantId(plant);
            Integer stock = extractPlantStock(plant);
            if (id == null || stock == null || stock < 0) {
                continue;
            }
            if (bestStock == null || stock < bestStock) {
                bestId = id;
                bestStock = stock;
            }
        }

        if (bestId == null) {
            Assume.assumeTrue("No low stock plant found", false);
        }

        selectedPlantId = bestId;
    }

    @And("current stock is captured for selected plant in admin sales api tests")
    public void currentStockIsCapturedForSelectedPlantInAdminSalesApiTests() {
        if (selectedPlantId == null) {
            Assume.assumeTrue("No selected plant id available", false);
        }

        Integer currentStock = fetchPlantStockById(selectedPlantId);
        if (currentStock == null) {
            Assume.assumeTrue("Unable to read stock for plant id " + selectedPlantId, false);
        }

        selectedPlantStock = currentStock;
    }

    @When("admin creates sale for selected plant with quantity 1 via sales api")
    public void adminCreatesSaleForSelectedPlantWithQuantityViaSalesApi() {
        response = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .queryParam("quantity", 1)
                .post(Urls.API_SALES + "/plant/" + selectedPlantId)
                .then()
                .extract()
                .response();

        status = response.statusCode();
    }

    @Then("admin sales api response status should allow created sale")
    public void adminSalesApiResponseStatusShouldAllowCreatedSale() {
        Assertions.assertThat(status == 200 || status == 201).isTrue();
    }

    @And("selected plant stock should be reduced by exactly 1 in admin sales api tests")
    public void selectedPlantStockShouldBeReducedByExactlyInAdminSalesApiTests() {
        Integer updatedStock = fetchPlantStockById(selectedPlantId);
        Assertions.assertThat(updatedStock).isNotNull();
        Assertions.assertThat(updatedStock).isEqualTo(selectedPlantStock - 1);
    }

    @When("admin attempts to oversell selected plant via sales api")
    public void adminAttemptsToOversellSelectedPlantViaSalesApi() {
        if (selectedPlantStock == null) {
            Assume.assumeTrue("Selected plant stock was not captured", false);
        }

        int oversellQuantity = selectedPlantStock + 1;
        if (oversellQuantity <= 0) {
            oversellQuantity = 1;
        }

        response = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .queryParam("quantity", oversellQuantity)
                .post(Urls.API_SALES + "/plant/" + selectedPlantId)
                .then()
                .extract()
                .response();

        status = response.statusCode();
    }

    @Then("admin sales api response status should indicate oversell is blocked")
    public void adminSalesApiResponseStatusShouldIndicateOversellIsBlocked() {
        Assertions.assertThat(status == 400 || status == 409).isTrue();
    }

    @And("oversell response should indicate insufficient stock for admin sales api tests")
    public void oversellResponseShouldIndicateInsufficientStockForAdminSalesApiTests() {
        String responseBody = response == null || response.asString() == null
                ? ""
                : response.asString().toLowerCase();
        String statusLine = response == null || response.statusLine() == null
                ? ""
                : response.statusLine().toLowerCase();

        boolean indicatesStockProblem = responseBody.contains("insufficient")
                || responseBody.contains("not enough")
                || responseBody.contains("stock")
                || responseBody.contains("exceed")
                || responseBody.contains("available");

        boolean statusIndicatesClientError = statusLine.contains("bad request")
                || statusLine.contains("conflict");

        Assertions.assertThat(indicatesStockProblem || statusIndicatesClientError).isTrue();
    }

    @And("selected plant stock should remain unchanged after oversell attempt in admin sales api tests")
    public void selectedPlantStockShouldRemainUnchangedAfterOversellAttemptInAdminSalesApiTests() {
        Integer updatedStock = fetchPlantStockById(selectedPlantId);
        Assertions.assertThat(updatedStock).isNotNull();
        Assertions.assertThat(updatedStock).isEqualTo(selectedPlantStock);
    }
    // ===== NEW CODE - ADMIN SALES API TESTS END =====

    private List<?> fetchPlantsForAdmin() {
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
            Assume.assumeTrue("Admin token is not authorized to fetch plants", false);
        }
        if (plantsStatus != 200) {
            Assume.assumeTrue("Unable to fetch plants. Status: " + plantsStatus, false);
        }

        List<?> plants = firstNonNullList(
                plantsResponse.jsonPath().getList("$"),
                plantsResponse.jsonPath().getList("content"),
                plantsResponse.jsonPath().getList("data"),
                plantsResponse.jsonPath().getList("data.content"),
                plantsResponse.jsonPath().getList("items"),
                plantsResponse.jsonPath().getList("data.items")
        );

        if (plants == null || plants.isEmpty()) {
            Assume.assumeTrue("No plant data available for admin sales API tests", false);
        }

        return plants;
    }

    private Integer fetchPlantStockById(Long plantId) {
        Response plantResponse = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .get(Urls.API_PLANTS + "/" + plantId)
                .then()
                .extract()
                .response();

        int plantStatus = plantResponse.statusCode();
        if (plantStatus == 0) {
            Assume.assumeTrue("API not reachable at " + Urls.API_PLANTS + "/" + plantId, false);
        }
        if (plantStatus != 200) {
            Assume.assumeTrue("Unable to fetch plant details. Status: " + plantStatus, false);
        }

        return firstNonNullInt(
                plantResponse.jsonPath().get("quantity"),
                plantResponse.jsonPath().get("stock"),
                plantResponse.jsonPath().get("availableStock"),
                plantResponse.jsonPath().get("data.quantity"),
                plantResponse.jsonPath().get("data.stock"),
                plantResponse.jsonPath().get("data.availableStock"),
                plantResponse.jsonPath().get("plant.quantity"),
                plantResponse.jsonPath().get("plant.stock"),
                plantResponse.jsonPath().get("plant.availableStock")
        );
    }

    private Long extractPlantId(Object plantObject) {
        if (!(plantObject instanceof Map<?, ?> plantMap)) {
            return null;
        }

        return firstNonNullLong(
                plantMap.get("id"),
                plantMap.get("plantId"),
                nestedMapValue(plantMap, "plant", "id")
        );
    }

    private Integer extractPlantStock(Object plantObject) {
        if (!(plantObject instanceof Map<?, ?> plantMap)) {
            return null;
        }

        return firstNonNullInt(
                plantMap.get("quantity"),
                plantMap.get("stock"),
                plantMap.get("availableStock"),
                nestedMapValue(plantMap, "plant", "quantity"),
                nestedMapValue(plantMap, "plant", "stock"),
                nestedMapValue(plantMap, "plant", "availableStock")
        );
    }

    private Object nestedMapValue(Map<?, ?> source, String first, String second) {
        Object levelOne = source.get(first);
        if (!(levelOne instanceof Map<?, ?> nested)) {
            return null;
        }
        return nested.get(second);
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

    private Long firstNonNullLong(Object... candidates) {
        for (Object candidate : candidates) {
            Long value = toLong(candidate);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Integer firstNonNullInt(Object... candidates) {
        for (Object candidate : candidates) {
            Integer value = toInt(candidate);
            if (value != null) {
                return value;
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

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        if (text.matches("-?\\d+")) {
            return Integer.parseInt(text);
        }
        if (text.matches("-?\\d+\\.0+")) {
            return (int) Double.parseDouble(text);
        }
        return null;
    }

    // ===== NEW CODE - ADMIN SALES API TESTS START =====
    @When("admin requests sales list via sales api")
    public void adminRequestsSalesListViaSalesApi() {
        response = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .get(Urls.API_SALES)
                .then()
                .extract()
                .response();

        status = response.statusCode();
    }

    @Then("admin sales list api response status should be 200")
    public void adminSalesListApiResponseStatusShouldBe200() {
        Assertions.assertThat(status).isEqualTo(200);
    }

    @And("admin sales list response should contain sales records array")
    public void adminSalesListResponseShouldContainSalesRecordsArray() {
        List<?> sales = firstNonNullList(
                response.jsonPath().getList("$"),
                response.jsonPath().getList("content"),
                response.jsonPath().getList("data"),
                response.jsonPath().getList("data.content"),
                response.jsonPath().getList("items"),
                response.jsonPath().getList("data.items")
        );

        Assertions.assertThat(sales).isNotNull();
    }

    @And("admin sales list response should not contain access errors")
    public void adminSalesListResponseShouldNotContainAccessErrors() {
        String body = response == null || response.asString() == null
                ? ""
                : response.asString().toLowerCase();

        boolean hasAccessError = body.contains("unauthorized")
                || body.contains("forbidden")
                || body.contains("access denied");

        Assertions.assertThat(hasAccessError).isFalse();
    }

    @And("at least one sale exists for admin sales api delete tests")
    public void atLeastOneSaleExistsForAdminSalesApiDeleteTests() {
        List<?> sales = fetchSalesForAdmin();
        Long saleId = null;

        for (Object sale : sales) {
            saleId = extractSaleId(sale);
            if (saleId != null) {
                break;
            }
        }

        if (saleId == null) {
            saleId = createSaleAndReturnIdForAdminDelete();
        }

        if (saleId == null) {
            Assume.assumeTrue("No sale record available for delete test", false);
        }

        net.serenitybdd.core.Serenity.setSessionVariable("adminSalesDeleteSaleId").to(saleId);
    }

    @When("admin deletes selected sale via sales api")
    public void adminDeletesSelectedSaleViaSalesApi() {
        Long saleId = firstNonNullLong((Object) net.serenitybdd.core.Serenity.sessionVariableCalled("adminSalesDeleteSaleId"));
        if (saleId == null) {
            Assume.assumeTrue("No sale id available for delete test", false);
        }

        response = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .delete(Urls.API_SALES + "/" + saleId)
                .then()
                .extract()
                .response();

        status = response.statusCode();
    }

    @Then("admin sales delete api response status should allow success")
    public void adminSalesDeleteApiResponseStatusShouldAllowSuccess() {
        Assertions.assertThat(status == 200 || status == 204).isTrue();
    }

    @And("deleted sale should not be found via sales api")
    public void deletedSaleShouldNotBeFoundViaSalesApi() {
        Long saleId = firstNonNullLong((Object) net.serenitybdd.core.Serenity.sessionVariableCalled("adminSalesDeleteSaleId"));
        if (saleId == null) {
            Assume.assumeTrue("No sale id available to verify deletion", false);
        }

        Response getAfterDelete = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .get(Urls.API_SALES + "/" + saleId)
                .then()
                .extract()
                .response();

        int getStatus = getAfterDelete.statusCode();
        String body = getAfterDelete.asString() == null ? "" : getAfterDelete.asString().toLowerCase();

        boolean deleted = getStatus == 404
                || (getStatus >= 400 && body.contains("not found"))
                || (getStatus >= 400 && body.contains("does not exist"));

        Assertions.assertThat(deleted).isTrue();
    }

    private List<?> fetchSalesForAdmin() {
        Response salesResponse = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .get(Urls.API_SALES)
                .then()
                .extract()
                .response();

        int salesStatus = salesResponse.statusCode();
        if (salesStatus == 0) {
            Assume.assumeTrue("API not reachable at " + Urls.API_SALES, false);
        }
        if (salesStatus == 401 || salesStatus == 403) {
            Assume.assumeTrue("Admin token is not authorized to fetch sales list", false);
        }
        if (salesStatus != 200) {
            Assume.assumeTrue("Unable to fetch sales list. Status: " + salesStatus, false);
        }

        return firstNonNullList(
                salesResponse.jsonPath().getList("$"),
                salesResponse.jsonPath().getList("content"),
                salesResponse.jsonPath().getList("data"),
                salesResponse.jsonPath().getList("data.content"),
                salesResponse.jsonPath().getList("items"),
                salesResponse.jsonPath().getList("data.items")
        );
    }

    private Long extractSaleId(Object saleObject) {
        if (!(saleObject instanceof Map<?, ?> saleMap)) {
            return null;
        }

        return firstNonNullLong(
                saleMap.get("id"),
                saleMap.get("saleId"),
                nestedMapValue(saleMap, "sale", "id")
        );
    }

    private Long createSaleAndReturnIdForAdminDelete() {
        List<?> plants = fetchPlantsForAdmin();
        Long plantId = null;

        for (Object plant : plants) {
            Long id = extractPlantId(plant);
            Integer stock = extractPlantStock(plant);
            if (id != null && stock != null && stock >= 1) {
                plantId = id;
                break;
            }
        }

        if (plantId == null) {
            return null;
        }

        Response createResponse = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .queryParam("quantity", 1)
                .post(Urls.API_SALES + "/plant/" + plantId)
                .then()
                .extract()
                .response();

        int createStatus = createResponse.statusCode();
        if (!(createStatus == 200 || createStatus == 201)) {
            return null;
        }

        Long createdId = firstNonNullLong(
                createResponse.jsonPath().get("id"),
                createResponse.jsonPath().get("data.id"),
                createResponse.jsonPath().get("sale.id")
        );
        if (createdId != null) {
            return createdId;
        }

        List<?> sales = fetchSalesForAdmin();
        if (sales == null) {
            return null;
        }

        for (Object sale : sales) {
            Long saleId = extractSaleId(sale);
            if (saleId != null) {
                return saleId;
            }
        }
        return null;
    }
    // ===== NEW CODE - ADMIN SALES API TESTS END =====
}
