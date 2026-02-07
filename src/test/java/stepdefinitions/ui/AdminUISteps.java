package stepdefinitions.ui;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.thucydides.core.pages.Pages;
import org.assertj.core.api.Assertions;
import pages.CategoriesPage;
import pages.PlantsPage;
import pages.SalesPage;
import utils.Urls;

public class AdminUISteps {

    private final CategoriesPage categoriesPage;
    private final PlantsPage plantsPage;
    private final SalesPage salesPage;

    private String createdCategoryName;
    private String editedCategoryName;
    private String createdPlantName;

    public AdminUISteps(Pages pages) {
        this.categoriesPage = pages.getPage(CategoriesPage.class);
        this.plantsPage = pages.getPage(PlantsPage.class);
        this.salesPage = pages.getPage(SalesPage.class);
    }

    @When("admin opens categories page")
    public void adminOpensCategoriesPage() {
        categoriesPage.openUrl(Urls.UI_CATEGORIES);
    }

    @When("admin adds a main category")
    public void adminAddsMainCategory() {
        createdCategoryName = "CAT" + (System.currentTimeMillis() % 100000);
        categoriesPage.openAddCategory();
        categoriesPage.setCategoryName(createdCategoryName);
        categoriesPage.selectNoParentIfPossible();
        categoriesPage.saveCategory();
    }

    @Then("category should appear in list")
    public void categoryShouldAppearInList() {
        Assertions.assertThat(categoriesPage.isListVisible())
                .as("Category list should be visible")
                .isTrue();
        Assertions.assertThat(categoriesPage.listContains(createdCategoryName)).isTrue();
    }

    @When("admin edits a category name")
    public void adminEditsCategoryName() {
        String baseName = "C" + (System.currentTimeMillis() % 100000);
        categoriesPage.openAddCategory();
        categoriesPage.setCategoryName(baseName);
        categoriesPage.selectNoParentIfPossible();
        categoriesPage.saveCategory();

        editedCategoryName = "E" + (System.currentTimeMillis() % 100000);
        categoriesPage.enterSearchTerm(baseName);
        categoriesPage.applySearch();
        boolean editOpened = categoriesPage.openEditForName(baseName);
        Assertions.assertThat(editOpened).as("Edit action should be available for category").isTrue();
        categoriesPage.setCategoryName(editedCategoryName);
        categoriesPage.saveCategory();
    }

    @Then("category update should be visible")
    public void categoryUpdateShouldBeVisible() {
        Assertions.assertThat(categoriesPage.isListVisible())
                .as("Category list should be visible")
                .isTrue();
        Assertions.assertThat(categoriesPage.listContains(editedCategoryName)).isTrue();
    }

    @When("admin deletes a category with no dependencies")
    public void adminDeletesCategoryWithNoDependencies() {
        createdCategoryName = "D" + (System.currentTimeMillis() % 100000);
        categoriesPage.openAddCategory();
        categoriesPage.setCategoryName(createdCategoryName);
        categoriesPage.selectNoParentIfPossible();
        categoriesPage.saveCategory();
        categoriesPage.enterSearchTerm(createdCategoryName);
        categoriesPage.applySearch();
        boolean deleted = categoriesPage.deleteByName(createdCategoryName);
        if (!deleted) {
            boolean editOpened = categoriesPage.openEditForName(createdCategoryName);
            Assertions.assertThat(editOpened).as("Edit action should be available for category").isTrue();
            deleted = categoriesPage.deleteFromEditPage();
        }
        Assertions.assertThat(deleted).as("Delete action should be available for category").isTrue();
    }

    @Then("category should be removed from list")
    public void categoryShouldBeRemovedFromList() {
        Assertions.assertThat(categoriesPage.isListVisible())
                .as("Category list should be visible")
                .isTrue();
        Assertions.assertThat(categoriesPage.listContains(createdCategoryName)).isFalse();
    }

    @When("admin opens plants page")
    public void adminOpensPlantsPage() {
        plantsPage.openUrl(Urls.UI_PLANTS);
    }

    @When("admin adds a plant under a sub-category")
    public void adminAddsPlantUnderSubCategory() {
        createdPlantName = "P" + (System.currentTimeMillis() % 100000);
        String parentName = "P" + (System.currentTimeMillis() % 100000);
        String subName = "S" + (System.currentTimeMillis() % 100000);

        categoriesPage.openUrl(Urls.UI_CATEGORIES);
        categoriesPage.openAddCategory();
        categoriesPage.setCategoryName(parentName);
        categoriesPage.selectNoParentIfPossible();
        categoriesPage.saveCategory();

        categoriesPage.openAddCategory();
        categoriesPage.setCategoryName(subName);
        boolean parentSelected = categoriesPage.selectParentByName(parentName);
        Assertions.assertThat(parentSelected).as("Parent category should be selectable").isTrue();
        categoriesPage.saveCategory();

        plantsPage.openUrl(Urls.UI_PLANTS_ADD);
        plantsPage.setPlantName(createdPlantName);
        plantsPage.setPlantPriceIfPresent("10");
        plantsPage.setPlantQuantityIfPresent("1");
        boolean selected = plantsPage.selectCategoryByName(subName);
        if (!selected) {
            // refresh once to allow dropdown to repopulate after category creation
            plantsPage.openUrl(Urls.UI_PLANTS_ADD);
            plantsPage.setPlantName(createdPlantName);
            plantsPage.setPlantPriceIfPresent("10");
            plantsPage.setPlantQuantityIfPresent("1");
            selected = plantsPage.selectCategoryByName(subName);
        }
        Assertions.assertThat(selected).as("Sub-category option should be selectable").isTrue();
        plantsPage.savePlant();
    }

    @Then("plant should appear in list")
    public void plantShouldAppearInList() {
        // Clear any previous filters from other scenarios
        plantsPage.resetToPlantsList();
        plantsPage.enterListSearchTerm(createdPlantName);
        plantsPage.applyFilterIfPresent();

        Assertions.assertThat(plantsPage.isAt()).isTrue();

        // Wait a bit for the list to render + plant to appear
        boolean visible = plantsPage.waitUntilPlantVisible(createdPlantName);
        if (!visible) {
            String firstName = plantsPage.getFirstListedPlantName();
            visible = createdPlantName != null && createdPlantName.equals(firstName);
        }
        Assertions.assertThat(visible).as("Plant should appear in list").isTrue();
    }

    @When("admin opens sales page")
    public void adminOpensSalesPage() {
        salesPage.openUrl(Urls.UI_SALES);
    }

    @Then("sales list should be visible")
    public void salesListShouldBeVisible() {
        Assertions.assertThat(salesPage.isListVisible() || salesPage.isAt()).isTrue();
    }

    @Then("sell plant action should be visible")
    public void sellPlantActionShouldBeVisible() {
        Assertions.assertThat(salesPage.sellActionVisible()).isTrue();
    }
}
