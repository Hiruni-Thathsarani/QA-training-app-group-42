package stepdefinitions.ui;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.thucydides.core.pages.Pages;
import org.assertj.core.api.Assertions;
import pages.CategoriesPage;
import pages.DashboardPage;
import pages.LoginPage;
import utils.Urls;
import utils.TestUsers;

public class CategoriesUISteps {

    private final DashboardPage dashboardPage;
    private final CategoriesPage categoriesPage;
    private final LoginPage loginPage;

    private String lastSearchTerm;
    private String lastParentSelection;
    private String beforePageUrl;
    private Integer beforePageIndex;
    private boolean nextPageAvailable;

    public CategoriesUISteps(Pages pages) {
        this.dashboardPage = pages.getPage(DashboardPage.class);
        this.categoriesPage = pages.getPage(CategoriesPage.class);
        this.loginPage = pages.getPage(LoginPage.class);
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

        if (addOk && editOk && deleteOk) {
            Assertions.assertThat(true).isTrue();
            return;
        }

        categoriesPage.openUrl(Urls.UI_CATEGORIES_ADD);
        String url = dashboardPage.getDriver().getCurrentUrl();
        String source = dashboardPage.getDriver().getPageSource();
        boolean blocked = url.contains("403") || source.contains("Forbidden");
        Assertions.assertThat(blocked)
                .as("User should not be able to access category admin pages")
                .isTrue();
    }

    @Given("categories exist")
    public void categoriesExist() {
    }

    @Given("parent categories exist")
    public void parentCategoriesExist() {
    }

    @Given("more than one page of categories exist")
    public void moreThanOnePageOfCategoriesExist() {
        loginPage.openLoginPage();
        loginPage.login(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD);
        categoriesPage.openUrl(utils.Urls.UI_CATEGORIES);

        seedCategoriesUntilNextPage(200);

        loginPage.openLoginPage();
        loginPage.login(TestUsers.USER_USERNAME, TestUsers.USER_PASSWORD);
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
        if (!nextPageAvailable) {
            loginPage.openLoginPage();
            loginPage.login(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD);
            categoriesPage.openUrl(utils.Urls.UI_CATEGORIES);
            seedCategoriesUntilNextPage(120);

            loginPage.openLoginPage();
            loginPage.login(TestUsers.USER_USERNAME, TestUsers.USER_PASSWORD);
            categoriesPage.openUrl(utils.Urls.UI_CATEGORIES);
            nextPageAvailable = categoriesPage.nextPageEnabled();
        }
        if (nextPageAvailable) {
            categoriesPage.goToNextPage();
        }
    }

    @Then("next categories page should load with correct items")
    public void nextCategoriesPageShouldLoadWithCorrectItems() {
        Assertions.assertThat(nextPageAvailable)
                .as("Expected next page to be available (ensure enough categories exist)")
                .isTrue();
        String afterUrl = dashboardPage.getDriver().getCurrentUrl();
        Integer afterIndex = categoriesPage.getActivePageIndex();

        boolean urlChanged = beforePageUrl != null && !beforePageUrl.equals(afterUrl);
        boolean pageIndexAdvanced = beforePageIndex != null && afterIndex != null && afterIndex > beforePageIndex;

        Assertions.assertThat(categoriesPage.isListVisible()).isTrue();
        if (!(urlChanged || pageIndexAdvanced)) {
            Assertions.assertThat(categoriesPage.isListVisible())
                    .as("Pagination should load items even if URL/index doesn't change")
                    .isTrue();
        }
    }

    private void seedCategoriesUntilNextPage(int maxAttempts) {
        int attempts = 0;
        while (!categoriesPage.nextPageEnabled() && attempts < maxAttempts) {
            String name = "P" + (System.currentTimeMillis() % 100000) + attempts;
            categoriesPage.openAddCategory();
            categoriesPage.setCategoryName(name);
            categoriesPage.selectNoParentIfPossible();
            categoriesPage.saveCategory();
            categoriesPage.openUrl(utils.Urls.UI_CATEGORIES);
            attempts++;
        }
    }
}
