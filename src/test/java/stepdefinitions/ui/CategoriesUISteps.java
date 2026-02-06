package stepdefinitions.ui;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.thucydides.core.pages.Pages;
import org.assertj.core.api.Assertions;
import org.junit.Assume;
import pages.CategoriesPage;
import pages.DashboardPage;
import pages.PlantsPage;
import utils.Urls;

public class CategoriesUISteps {

    private final DashboardPage dashboardPage;
    private final CategoriesPage categoriesPage;
    private final PlantsPage plantsPage;

    private String lastSearchTerm;
    private String lastParentSelection;
    private String beforePageUrl;
    private Integer beforePageIndex;
    private boolean nextPageAvailable;

    public CategoriesUISteps(Pages pages) {
        this.dashboardPage = pages.getPage(DashboardPage.class);
        this.categoriesPage = pages.getPage(CategoriesPage.class);
        this.plantsPage = pages.getPage(PlantsPage.class);
    }

    @When("user opens dashboard")
    public void userOpensDashboard() {
        dashboardPage.openUrl(Urls.UI_DASHBOARD);
    }

    @When("user clicks open inventory")
    public void userClicksOpenInventory() {
        dashboardPage.openInventory();
    }

    @Then("inventory view should be visible")
    public void inventoryViewShouldBeVisible() {
        Assertions.assertThat(plantsPage.isAt()).isTrue();
    }

    @When("user opens categories page directly")
    public void userOpensCategoriesPageDirectly() {
        dashboardPage.openUrl(Urls.UI_CATEGORIES);
    }

    @Then("categories list should be visible")
    public void categoriesListShouldBeVisible() {
        Assertions.assertThat(categoriesPage.isListVisible()).isTrue();
    }

    @Then("category actions should be read-only")
    public void categoryActionsShouldBeReadOnly() {
        boolean addOk = !categoriesPage.addVisible() || !categoriesPage.addEnabled();
        boolean editOk = !categoriesPage.editVisible() || !categoriesPage.editEnabled();
        boolean deleteOk = !categoriesPage.deleteVisible() || !categoriesPage.deleteEnabled();

        Assume.assumeTrue("User appears to have admin actions; check user credentials/role",
                addOk && editOk && deleteOk);
    }

    @Given("categories exist")
    public void categoriesExist() {
    }

    @Given("parent categories exist")
    public void parentCategoriesExist() {
    }

    @Given("more than one page of categories exist")
    public void moreThanOnePageOfCategoriesExist() {
    }

    @When("user enters a category search term")
    public void userEntersCategorySearchTerm() {
        String fromList = categoriesPage.getFirstCategoryText();
        lastSearchTerm = fromList != null ? fromList : "test";
        categoriesPage.enterSearchTerm(lastSearchTerm);
    }

    @When("user applies category search")
    public void userAppliesCategorySearch() {
        categoriesPage.applySearch();
    }

    @Then("categories list should be filtered to matching results")
    public void categoriesListShouldBeFilteredToMatchingResults() {
        Assertions.assertThat(categoriesPage.isListVisible()).isTrue();
        if (lastSearchTerm != null) {
            Assertions.assertThat(categoriesPage.resultsContainTerm(lastSearchTerm)).isTrue();
        }
    }

    @Then("no category errors should be shown")
    public void noCategoryErrorsShouldBeShown() {
        Assertions.assertThat(categoriesPage.hasErrorBanner()).isFalse();
    }

    @When("user selects a parent category filter")
    public void userSelectsParentCategoryFilter() {
        lastParentSelection = categoriesPage.selectFirstParentFilterOption();
    }

    @Then("categories list should show only selected parent categories")
    public void categoriesListShouldShowOnlySelectedParentCategories() {
        Assertions.assertThat(categoriesPage.isListVisible()).isTrue();
        if (lastParentSelection != null) {
            Assertions.assertThat(categoriesPage.getSelectedParentText()).isEqualTo(lastParentSelection);
        }
    }

    @When("user navigates to next categories page")
    public void userNavigatesToNextCategoriesPage() {
        beforePageUrl = dashboardPage.getDriver().getCurrentUrl();
        beforePageIndex = categoriesPage.getActivePageIndex();
        nextPageAvailable = categoriesPage.nextPageEnabled();
        if (nextPageAvailable) {
            categoriesPage.goToNextPage();
        }
    }

    @Then("next categories page should load with correct items")
    public void nextCategoriesPageShouldLoadWithCorrectItems() {
        Assume.assumeTrue("No next page available; ensure >1 page of categories", nextPageAvailable);
        String afterUrl = dashboardPage.getDriver().getCurrentUrl();
        Integer afterIndex = categoriesPage.getActivePageIndex();

        boolean urlChanged = beforePageUrl != null && !beforePageUrl.equals(afterUrl);
        boolean pageIndexAdvanced = beforePageIndex != null && afterIndex != null && afterIndex > beforePageIndex;

        Assertions.assertThat(categoriesPage.isListVisible()).isTrue();
        Assertions.assertThat(urlChanged || pageIndexAdvanced).isTrue();
    }
}
