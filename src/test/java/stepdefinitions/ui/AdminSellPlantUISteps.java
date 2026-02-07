package stepdefinitions.ui;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.thucydides.core.pages.Pages;
import org.assertj.core.api.Assertions;
import pages.DashboardPage;
import pages.SalesPage;
import utils.Urls;

public class AdminSellPlantUISteps {

    private final DashboardPage dashboardPage;
    private final SalesPage salesPage;

    // ===== NEW CODE - USER SALES LIST TESTS START =====
    private boolean plantOptionAvailable;
    // ===== NEW CODE - USER SALES LIST TESTS END =====

    public AdminSellPlantUISteps(Pages pages) {
        this.dashboardPage = pages.getPage(DashboardPage.class);
        this.salesPage = pages.getPage(SalesPage.class);
    }

    // ===== NEW CODE - USER SALES LIST TESTS START =====
    @When("admin opens sell plant page directly")
    public void adminOpensSellPlantPageDirectly() {
        dashboardPage.openUrl(Urls.UI_SELL_NEW);
    }

    @Given("sell plant form is open for admin")
    public void sellPlantFormIsOpenForAdmin() {
        dashboardPage.openUrl(Urls.UI_SELL_NEW);
        Assertions.assertThat(salesPage.isSellPlantFormVisible()).isTrue();
    }

    @Then("sell plant form should be visible for admin")
    public void sellPlantFormShouldBeVisibleForAdmin() {
        Assertions.assertThat(salesPage.isSellPlantFormVisible()).isTrue();
    }

    @Then("sell plant required fields should be visible")
    public void sellPlantRequiredFieldsShouldBeVisible() {
        Assertions.assertThat(salesPage.plantSelectionVisible()).isTrue();
        Assertions.assertThat(salesPage.quantityFieldVisible()).isTrue();
        Assertions.assertThat(salesPage.submitSellVisible()).isTrue();
    }

    @Then("no sell plant access errors should be shown")
    public void noSellPlantAccessErrorsShouldBeShown() {
        Assertions.assertThat(salesPage.hasAccessOrAuthorizationError()).isFalse();
    }

    @When("admin submits sell form without quantity")
    public void adminSubmitsSellFormWithoutQuantity() {
        salesPage.selectFirstPlantForSale();
        salesPage.clearQuantityForSale();
        salesPage.submitSellForm();
    }

    @Then("quantity validation message should be shown")
    public void quantityValidationMessageShouldBeShown() {
        Assertions.assertThat(salesPage.quantityValidationErrorVisible()).isTrue();
    }

    @Then("sale should not be created from sell plant form")
    public void saleShouldNotBeCreatedFromSellPlantForm() {
        Assertions.assertThat(salesPage.saleCreationSuccessVisible()).isFalse();
    }

    @Then("admin should remain on sell plant form")
    public void adminShouldRemainOnSellPlantForm() {
        Assertions.assertThat(salesPage.isAtSellPlantPage()).isTrue();
        Assertions.assertThat(salesPage.isSellPlantFormVisible()).isTrue();
    }

    @Given("limited stock plant is available for selling")
    public void limitedStockPlantIsAvailableForSelling() {
        plantOptionAvailable = salesPage.selectFirstPlantForSale();
        org.junit.Assume.assumeTrue("No selectable plant available on Sell Plant form", plantOptionAvailable);
    }

    @When("admin submits quantity greater than available stock")
    public void adminSubmitsQuantityGreaterThanAvailableStock() {
        salesPage.enterOversellQuantity();
        salesPage.submitSellForm();
    }

    @Then("insufficient stock message should be shown")
    public void insufficientStockMessageShouldBeShown() {
        Assertions.assertThat(salesPage.insufficientStockErrorVisible()).isTrue();
    }

    @Then("no redirect to sales list should occur")
    public void noRedirectToSalesListShouldOccur() {
        Assertions.assertThat(salesPage.isAtSellPlantPage()).isTrue();
    }
    // ===== NEW CODE - USER SALES LIST TESTS END =====
}
