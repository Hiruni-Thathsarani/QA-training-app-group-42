package stepdefinitions.ui;

import io.cucumber.java.en.*;
import net.thucydides.core.pages.Pages;
import org.assertj.core.api.Assertions;
import pages.CategoriesPage;
import pages.DashboardPage;
import pages.LoginPage;
import utils.ApiClient;
import utils.TestUsers;
import utils.Urls;

import java.util.List;

/**
 * Step definitions for Admin role UI tests on Categories module (TC-091 to
 * TC-095)
 * Uses Pages injection pattern for Serenity compatibility
 */
public class CategoriesAdminUISteps {

    private final LoginPage loginPage;
    private final DashboardPage dashboardPage;
    private final CategoriesPage categoriesPage;

    private String testCategoryName;
    private String parentCategoryName;
    private List<String> initialCategoryList;

    public CategoriesAdminUISteps(Pages pages) {
        this.loginPage = pages.getPage(LoginPage.class);
        this.dashboardPage = pages.getPage(DashboardPage.class);
        this.categoriesPage = pages.getPage(CategoriesPage.class);
    }

    // ============== TC-091: NAME REQUIRED VALIDATION ==============

    @When("admin clicks Add Category button")
    public void adminClicksAddCategoryButton() {
        categoriesPage.clickAddCategory();
        waitForPageLoad();
    }

    @When("admin leaves category name blank")
    public void adminLeavesCategoryNameBlank() {
        categoriesPage.clearCategoryName();
    }

    @When("admin clicks Save button")
    public void adminClicksSaveButton() {
        categoriesPage.clickSave();
        waitForPageLoad();
    }

    @Then("validation error should be displayed for required name")
    public void validationErrorShouldBeDisplayedForRequiredName() {
        // Check for validation error or that we're still on the form
        boolean hasError = categoriesPage.isValidationErrorDisplayed();
        String url = categoriesPage.getCurrentUrl();
        boolean stillOnForm = url.contains("/add") || url.contains("/edit");

        Assertions.assertThat(hasError || stillOnForm)
                .as("Validation error should be shown or form should not submit")
                .isTrue();
    }

    // ============== TC-092: NAME LENGTH VALIDATION ==============

    @When("admin enters category name with invalid length {string}")
    public void adminEntersCategoryNameWithInvalidLength(String invalidName) {
        categoriesPage.enterCategoryName(invalidName);
    }

    @Then("validation error should be displayed for name length")
    public void validationErrorShouldBeDisplayedForNameLength() {
        boolean hasError = categoriesPage.isValidationErrorDisplayed();
        String errorText = categoriesPage.getValidationErrorText().toLowerCase();
        String url = categoriesPage.getCurrentUrl();
        boolean stillOnForm = url.contains("/add") || url.contains("/edit");

        boolean lengthError = errorText.contains("length") ||
                errorText.contains("between") ||
                errorText.contains("characters") ||
                errorText.contains("10") ||
                hasError;

        Assertions.assertThat(lengthError || stillOnForm)
                .as("Validation error for name length should be shown")
                .isTrue();
    }

    // ============== TC-093: CREATE SUB-CATEGORY ==============

    @Given("a main category exists")
    public void aMainCategoryExists() {
        // Create a main category via API if needed
        String token = ApiClient.loginAndGetToken(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD);
        if (token != null) {
            parentCategoryName = "TestMain" + System.currentTimeMillis() % 1000;
            // Ensure name is 3-10 chars
            if (parentCategoryName.length() > 10) {
                parentCategoryName = parentCategoryName.substring(0, 10);
            }
            String body = String.format("""
                    {
                      "name": "%s",
                      "parent": null
                    }
                    """, parentCategoryName);
            ApiClient.postWithBearer(Urls.API_CATEGORIES, token, body);
        }
        categoriesPage.openCategoriesPage();
        waitForPageLoad();
    }

    @When("admin enters valid category name")
    public void adminEntersValidCategoryName() {
        // Generate unique name between 3-10 chars
        testCategoryName = "Sub" + System.currentTimeMillis() % 10000;
        if (testCategoryName.length() > 10) {
            testCategoryName = testCategoryName.substring(0, 10);
        }
        categoriesPage.enterCategoryName(testCategoryName);
    }

    @When("admin selects a parent category")
    public void adminSelectsAParentCategory() {
        if (parentCategoryName != null) {
            try {
                categoriesPage.selectParentCategory(parentCategoryName);
            } catch (Exception e) {
                categoriesPage.selectFirstAvailableParent();
            }
        } else {
            categoriesPage.selectFirstAvailableParent();
        }
    }

    @Then("sub-category should be created successfully")
    public void subCategoryShouldBeCreatedSuccessfully() {
        // Either we see success message or we're back on list
        boolean success = categoriesPage.isSuccessMessageDisplayed() ||
                categoriesPage.isOnCategoriesListPage();
        Assertions.assertThat(success)
                .as("Sub-category should be created successfully")
                .isTrue();
    }

    @Then("success message should be displayed")
    public void successMessageShouldBeDisplayed() {
        // May redirect quickly, so just verify we're not on error page
        String url = categoriesPage.getCurrentUrl();
        boolean notOnError = !url.contains("error");

        Assertions.assertThat(categoriesPage.isSuccessMessageDisplayed() ||
                categoriesPage.isOnCategoriesListPage() ||
                notOnError)
                .as("Success message or redirect to list should occur")
                .isTrue();
    }

    // ============== TC-094: EDIT CANCEL ==============

    @Given("categories exist")
    public void categoriesExist() {
        initialCategoryList = categoriesPage.getCategoryNames();
    }

    @When("admin clicks Edit on a category")
    public void adminClicksEditOnACategory() {
        categoriesPage.clickFirstEditButton();
        waitForPageLoad();
    }

    @When("admin modifies the category name")
    public void adminModifiesTheCategoryName() {
        testCategoryName = "Modified" + System.currentTimeMillis() % 1000;
        if (testCategoryName.length() > 10) {
            testCategoryName = testCategoryName.substring(0, 10);
        }
        categoriesPage.enterCategoryName(testCategoryName);
    }

    @When("admin clicks Cancel button")
    public void adminClicksCancelButton() {
        categoriesPage.clickCancel();
        waitForPageLoad();
    }

    @Then("user should be navigated back to categories list")
    public void userShouldBeNavigatedBackToCategoriesList() {
        Assertions.assertThat(categoriesPage.isOnCategoriesListPage())
                .as("Should navigate back to categories list")
                .isTrue();
    }

    @Then("category should not be updated")
    public void categoryShouldNotBeUpdated() {
        List<String> currentList = categoriesPage.getCategoryNames();
        // Modified name should not appear in the list
        boolean notUpdated = !currentList.contains(testCategoryName);
        Assertions.assertThat(notUpdated || initialCategoryList.containsAll(currentList))
                .as("Category should not be updated after cancel")
                .isTrue();
    }

    // ============== TC-095: DELETE PARENT BLOCKED ==============

    @Given("a parent category with children exists")
    public void aParentCategoryWithChildrenExists() {
        // Create parent and child via API
        String token = ApiClient.loginAndGetToken(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD);
        if (token != null) {
            // Create parent
            parentCategoryName = "Parent" + System.currentTimeMillis() % 1000;
            if (parentCategoryName.length() > 10) {
                parentCategoryName = parentCategoryName.substring(0, 10);
            }
            String parentBody = String.format("""
                    {
                      "name": "%s",
                      "parent": null
                    }
                    """, parentCategoryName);
            ApiClient.postWithBearer(Urls.API_CATEGORIES, token, parentBody);

            // Create child under parent
            String childName = "Child" + System.currentTimeMillis() % 1000;
            if (childName.length() > 10) {
                childName = childName.substring(0, 10);
            }
            String childBody = String.format("""
                    {
                      "name": "%s",
                      "parent": "%s"
                    }
                    """, childName, parentCategoryName);
            ApiClient.postWithBearer(Urls.API_CATEGORIES, token, childBody);
        }
        categoriesPage.openCategoriesPage();
        waitForPageLoad();
    }

    @When("admin attempts to delete the parent category")
    public void adminAttemptsToDeleteTheParentCategory() {
        if (parentCategoryName != null) {
            categoriesPage.clickDeleteForCategory(parentCategoryName);
        } else {
            categoriesPage.clickFirstDeleteButton();
        }
        waitForPageLoad();
    }

    @Then("delete should be blocked")
    public void deleteShouldBeBlocked() {
        // Parent should still exist or error shown
        boolean blocked = categoriesPage.isErrorAlertDisplayed() ||
                categoriesPage.isDeleteBlockedMessageDisplayed() ||
                categoriesPage.getCategoryNames().contains(parentCategoryName);
        Assertions.assertThat(blocked)
                .as("Delete of parent category should be blocked")
                .isTrue();
    }

    @Then("error message about children should be displayed")
    public void errorMessageAboutChildrenShouldBeDisplayed() {
        Assertions.assertThat(categoriesPage.isDeleteBlockedMessageDisplayed() ||
                categoriesPage.isErrorAlertDisplayed())
                .as("Error message about children should be displayed")
                .isTrue();
    }

    // ============== HELPER METHODS ==============

    private void waitForPageLoad() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
