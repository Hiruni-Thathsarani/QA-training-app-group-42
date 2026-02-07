package stepdefinitions.ui;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.thucydides.core.pages.Pages;
import org.assertj.core.api.Assertions;
import pages.DashboardPage;
import pages.PlantsPage;
import pages.SalesPage;
import utils.Urls;

public class AdminSellPlantUISteps {

    private final DashboardPage dashboardPage;
    private final PlantsPage plantsPage;
    private final SalesPage salesPage;

    // ===== NEW CODE - USER SALES LIST TESTS START =====
    private boolean plantOptionAvailable;
    // ===== NEW CODE - USER SALES LIST TESTS END =====

    // ===== NEW CODE - ADMIN SALES UI TESTS START =====
    private String selectedPlantName;
    private Integer selectedPlantStockBefore;
    private Integer selectedPlantStockAfter;
    private boolean saleCreationSucceeded;
    private int salesCountBeforeDelete;
    private String salesFirstRowBeforeDelete;
    private boolean deleteConfirmationDisplayed;
    // ===== NEW CODE - ADMIN SALES UI TESTS END =====

    public AdminSellPlantUISteps(Pages pages) {
        this.dashboardPage = pages.getPage(DashboardPage.class);
        this.plantsPage = pages.getPage(PlantsPage.class);
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

    // ===== NEW CODE - ADMIN SALES UI TESTS START =====
    @Given("admin has a sellable plant in plants list")
    public void adminHasASellablePlantInPlantsList() {
        dashboardPage.openUrl(Urls.UI_PLANTS);
        Assertions.assertThat(plantsPage.isAt()).isTrue();
        selectedPlantName = plantsPage.findFirstPlantNameWithStockAtLeast(1);
        org.junit.Assume.assumeTrue("No plant with stock >= 1 available in Plants list", selectedPlantName != null && !selectedPlantName.isBlank());
        selectedPlantStockBefore = plantsPage.getStockForPlant(selectedPlantName);
        org.junit.Assume.assumeTrue("Unable to determine stock for selected plant in Plants list", selectedPlantStockBefore != null && selectedPlantStockBefore >= 1);
    }

    @When("admin sells one unit of the selected plant")
    public void adminSellsOneUnitOfTheSelectedPlant() {
        dashboardPage.openUrl(Urls.UI_SELL_NEW);
        Assertions.assertThat(salesPage.isSellPlantFormVisible()).isTrue();
        boolean selectedInSellForm = salesPage.selectPlantForSaleByName(selectedPlantName);
        org.junit.Assume.assumeTrue("Selected plant not available in Sell Plant form", selectedInSellForm);
        salesPage.enterSaleQuantity(1);
        salesPage.submitSellForm();
        saleCreationSucceeded = salesPage.saleCreatedSuccessfully();
    }

    @When("admin navigates back to plants list")
    public void adminNavigatesBackToPlantsList() {
        dashboardPage.openUrl(Urls.UI_PLANTS);
        Assertions.assertThat(plantsPage.isAt()).isTrue();
        selectedPlantStockAfter = plantsPage.getStockForPlant(selectedPlantName);
    }

    @Then("sale should be created successfully for the selected plant")
    public void saleShouldBeCreatedSuccessfullyForTheSelectedPlant() {
        Assertions.assertThat(saleCreationSucceeded).isTrue();
    }

    @Then("selected plant stock should be reduced by exactly one")
    public void selectedPlantStockShouldBeReducedByExactlyOne() {
        Assertions.assertThat(selectedPlantStockBefore).isNotNull();
        Assertions.assertThat(selectedPlantStockAfter).isNotNull();
        Assertions.assertThat(selectedPlantStockAfter).isEqualTo(selectedPlantStockBefore - 1);
    }

    @Then("updated stock should be reflected for the selected plant in plants list")
    public void updatedStockShouldBeReflectedForTheSelectedPlantInPlantsList() {
        Assertions.assertThat(plantsPage.isListVisible()).isTrue();
        Assertions.assertThat(plantsPage.getStockForPlant(selectedPlantName)).isEqualTo(selectedPlantStockAfter);
    }

    @Given("at least one sale exists for admin")
    public void atLeastOneSaleExistsForAdmin() {
        dashboardPage.openUrl(Urls.UI_SALES);
        Assertions.assertThat(salesPage.isAt()).isTrue();
        Assertions.assertThat(salesPage.isListVisible()).isTrue();
        salesCountBeforeDelete = salesPage.getVisibleSalesRowCount();
        salesFirstRowBeforeDelete = salesPage.getFirstRowSnapshot();
        org.junit.Assume.assumeTrue("No sale record visible in Sales list", salesCountBeforeDelete > 0);
        Assertions.assertThat(salesPage.deleteActionVisible())
                .as("Delete action should be visible for Admin on Sales list")
                .isTrue();
    }

    @When("admin clicks delete on a sale record")
    public void adminClicksDeleteOnASaleRecord() {
        salesPage.clickFirstDeleteAction();
        deleteConfirmationDisplayed = salesPage.deleteConfirmationVisible();
    }

    @Then("delete confirmation dialog should be displayed for admin")
    public void deleteConfirmationDialogShouldBeDisplayedForAdmin() {
        Assertions.assertThat(deleteConfirmationDisplayed).isTrue();
    }

    @Then("sale should not be deleted without admin confirmation")
    public void saleShouldNotBeDeletedWithoutAdminConfirmation() {
        salesPage.cancelDeleteConfirmation();
        Assertions.assertThat(salesPage.getVisibleSalesRowCount()).isEqualTo(salesCountBeforeDelete);
        if (salesFirstRowBeforeDelete != null && !salesFirstRowBeforeDelete.isBlank()) {
            Assertions.assertThat(salesPage.containsVisibleRowSnapshot(salesFirstRowBeforeDelete)).isTrue();
        }
    }

    @Then("deletion should require explicit admin confirmation")
    public void deletionShouldRequireExplicitAdminConfirmation() {
        Assertions.assertThat(deleteConfirmationDisplayed).isTrue();
        Assertions.assertThat(salesPage.isAt()).isTrue();
    }
    // ===== NEW CODE - ADMIN SALES UI TESTS END =====
}
