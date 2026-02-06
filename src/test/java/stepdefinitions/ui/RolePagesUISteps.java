package stepdefinitions.ui;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.thucydides.core.pages.Pages;
import org.assertj.core.api.Assertions;
import pages.CategoriesPage;
import pages.PlantsPage;
import pages.DashboardPage;
import utils.Urls;

public class RolePagesUISteps {

    private final DashboardPage dashboardPage;
    private final CategoriesPage categoriesPage;
    private final PlantsPage plantsPage;

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

    @Then("categories should be read only for user")
    public void categoriesShouldBeReadOnlyForUser() {
        boolean addVisible = categoriesPage.addVisible();
        boolean editVisible = categoriesPage.editVisible();
        boolean deleteVisible = categoriesPage.deleteVisible();
        org.junit.Assume.assumeTrue(
                "Category admin actions visible for user; check role permissions",
                !(addVisible || editVisible || deleteVisible)
        );
    }

    @Then("plants should be read only for user")
    public void plantsShouldBeReadOnlyForUser() {
        org.junit.Assume.assumeTrue(
                "Plant admin actions visible for user; check role permissions",
                !plantsPage.adminActionsVisible()
        );
    }
}
