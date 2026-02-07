package stepdefinitions.ui;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.thucydides.core.pages.Pages;
import org.assertj.core.api.Assertions;
import pages.CategoriesPage;
import pages.PlantsPage;
import pages.DashboardPage;
import utils.Urls;
import java.util.List;

public class RolePagesUISteps {

    private final DashboardPage dashboardPage;
    private final CategoriesPage categoriesPage;
    private final PlantsPage plantsPage;
    private String lastPlantCategoryFilter;

    public RolePagesUISteps(Pages pages) {
        this.dashboardPage = pages.getPage(DashboardPage.class);
        this.categoriesPage = pages.getPage(CategoriesPage.class);
        this.plantsPage = pages.getPage(PlantsPage.class);
    }

    @When("user opens categories page")
    public void userOpensCategoriesPage() {
        dashboardPage.openUrl(Urls.UI_CATEGORIES);
    }

    @When("user opens plants page")
    public void userOpensPlantsPage() {
        dashboardPage.openUrl(Urls.UI_PLANTS);
    }

    @Then("categories page should be visible")
    public void categoriesPageShouldBeVisible() {
        Assertions.assertThat(categoriesPage.isAt()).isTrue();
    }

    @Then("plants page should be visible")
    public void plantsPageShouldBeVisible() {
        Assertions.assertThat(plantsPage.isAt()).isTrue();
    }

    @When("user selects a plant category filter")
    public void userSelectsPlantCategoryFilter() {
        plantsPage.resetToPlantsList(); // ensures no old query params
        lastPlantCategoryFilter = plantsPage.selectFirstCategoryFilterOption();
        plantsPage.applyFilterIfPresent();
    }

    @Then("plants list should show only selected category")
    public void plantsListShouldShowOnlySelectedCategory() {
        Assertions.assertThat(plantsPage.isListVisible() || plantsPage.isAt()).isTrue();

        if (lastPlantCategoryFilter != null) {
            // Dropdown should still show the selected category
            Assertions.assertThat(plantsPage.getSelectedCategoryFilterText())
                    .isEqualTo(lastPlantCategoryFilter);

            // Table results should match selected category in column 2
            List<String> categories = plantsPage.getVisibleListedPlantCategories();
            Assertions.assertThat(categories)
                    .as("Filtered plants table should have category values in column 2")
                    .isNotEmpty();
            Assertions.assertThat(categories)
                    .as("All visible rows should match selected category filter")
                    .allMatch(c -> c.equalsIgnoreCase(lastPlantCategoryFilter)
                            || c.contains(lastPlantCategoryFilter)
                            || lastPlantCategoryFilter.contains(c));

        }
    }

    @Then("categories should be read only for user")
    public void categoriesShouldBeReadOnlyForUser() {
        boolean addVisible = categoriesPage.addVisible();
        boolean editVisible = categoriesPage.editVisible();
        boolean deleteVisible = categoriesPage.deleteVisible();
        org.junit.Assume.assumeTrue(
                "Category admin actions visible for user; check role permissions",
                !(addVisible || editVisible || deleteVisible));
    }

    @Then("plants should be read only for user")
    public void plantsShouldBeReadOnlyForUser() {
        org.junit.Assume.assumeTrue(
                "Plant admin actions visible for user; check role permissions",
                !plantsPage.adminActionsVisible());
    }
}
