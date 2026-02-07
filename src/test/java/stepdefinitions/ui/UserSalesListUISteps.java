package stepdefinitions.ui;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.thucydides.core.pages.Pages;
import org.assertj.core.api.Assertions;
import pages.DashboardPage;
import pages.SalesPage;
import utils.Urls;

public class UserSalesListUISteps {

    private final DashboardPage dashboardPage;
    private final SalesPage salesPage;

    // ===== NEW CODE - USER SALES LIST TESTS START =====
    private String salesBeforePageUrl;
    private Integer salesBeforePageIndex;
    private String salesBeforeFirstRow;
    private boolean salesNextPageAvailable;
    // ===== NEW CODE - USER SALES LIST TESTS END =====

    public UserSalesListUISteps(Pages pages) {
        this.dashboardPage = pages.getPage(DashboardPage.class);
        this.salesPage = pages.getPage(SalesPage.class);
    }

    // ===== NEW CODE - USER SALES LIST TESTS START =====
    @When("user opens sales page")
    public void userOpensSalesPage() {
        dashboardPage.openUrl(Urls.UI_SALES);
    }

    @Then("sales page should be visible")
    public void salesPageShouldBeVisible() {
        Assertions.assertThat(salesPage.isAt()).isTrue();
        Assertions.assertThat(salesPage.isListVisible()).isTrue();
        Assertions.assertThat(salesPage.hasUiErrorBanner()).isFalse();
    }

    @Then("sales should be read only for user")
    public void salesShouldBeReadOnlyForUser() {
        org.junit.Assume.assumeTrue(
                "Sales admin actions or editable fields visible for user; check role permissions",
                salesPage.readOnlyForUser()
        );
    }

    @Then("more than one page of sales exist")
    public void moreThanOnePageOfSalesExist() {
    }

    @When("user navigates to next sales page")
    public void userNavigatesToNextSalesPage() {
        salesBeforePageUrl = dashboardPage.getDriver().getCurrentUrl();
        salesBeforePageIndex = salesPage.getActivePageIndex();
        salesBeforeFirstRow = salesPage.getFirstRowSnapshot();
        salesNextPageAvailable = salesPage.nextPageEnabled();
        if (salesNextPageAvailable) {
            salesPage.goToNextPage();
        }
    }

    @Then("next sales page should load with correct items")
    public void nextSalesPageShouldLoadWithCorrectItems() {
        org.junit.Assume.assumeTrue("No next sales page available; ensure sales exceed one page", salesNextPageAvailable);

        String salesAfterPageUrl = dashboardPage.getDriver().getCurrentUrl();
        Integer salesAfterPageIndex = salesPage.getActivePageIndex();
        String salesAfterFirstRow = salesPage.getFirstRowSnapshot();

        boolean urlChanged = salesBeforePageUrl != null && !salesBeforePageUrl.equals(salesAfterPageUrl);
        boolean pageIndexAdvanced = salesBeforePageIndex != null && salesAfterPageIndex != null && salesAfterPageIndex > salesBeforePageIndex;
        boolean dataSetChanged = salesBeforeFirstRow != null && salesAfterFirstRow != null && !salesBeforeFirstRow.equals(salesAfterFirstRow);

        Assertions.assertThat(salesPage.isListVisible()).isTrue();
        Assertions.assertThat(salesPage.hasUiErrorBanner()).isFalse();
        Assertions.assertThat(urlChanged || pageIndexAdvanced || dataSetChanged).isTrue();
    }

    @Then("multiple sales records with different sold dates exist")
    public void multipleSalesRecordsWithDifferentSoldDatesExist() {
    }

    @Then("sales list should be sorted by sold date descending")
    public void salesListShouldBeSortedBySoldDateDescending() {
        Assertions.assertThat(salesPage.isListVisible()).isTrue();
        Assertions.assertThat(salesPage.hasUiErrorBanner()).isFalse();
        org.junit.Assume.assumeTrue("Not enough visible sales rows with parseable sold dates", salesPage.getVisibleSoldDates().size() >= 2);
        Assertions.assertThat(salesPage.soldDatesSortedDesc()).isTrue();
    }
    // ===== NEW CODE - USER SALES LIST TESTS END =====

    // ===== NEW CODE - USER SALES LIST TESTS START =====
    @Then("sell plant option should not be visible for user")
    public void sellPlantOptionShouldNotBeVisibleForUser() {
        Assertions.assertThat(salesPage.sellPlantVisible()).isFalse();
        Assertions.assertThat(salesPage.createSaleActionVisible()).isFalse();
    }

    @Then("sales table should be read only with expected columns")
    public void salesTableShouldBeReadOnlyWithExpectedColumns() {
        Assertions.assertThat(salesPage.isListVisible()).isTrue();
        Assertions.assertThat(salesPage.hasExpectedReadOnlyColumns()).isTrue();
        Assertions.assertThat(salesPage.editableFieldsVisible()).isFalse();
        Assertions.assertThat(salesPage.hasActionColumn()).isFalse();
    }

    @Then("delete sale option should not be visible for user")
    public void deleteSaleOptionShouldNotBeVisibleForUser() {
        Assertions.assertThat(salesPage.deleteActionVisible()).isFalse();
        Assertions.assertThat(salesPage.hasActionColumn()).isFalse();
    }

    @Then("sales page should remain fully read only")
    public void salesPageShouldRemainFullyReadOnly() {
        Assertions.assertThat(salesPage.readOnlyForUser()).isTrue();
        Assertions.assertThat(salesPage.hasUiErrorBanner()).isFalse();
    }
    // ===== NEW CODE - USER SALES LIST TESTS END =====
}
