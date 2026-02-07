package pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import utils.Urls;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Page Object for Categories page with selectors for:
 * - List view (table, pagination, sorting, filtering)
 * - Add/Edit form (name input, parent dropdown, save/cancel)
 * - Validation messages
 */
public class CategoriesPage extends PageObject {

    // ============== LIST PAGE ELEMENTS ==============

    @FindBy(css = "h1, h2, .page-title")
    WebElementFacade title;

    // Search input - actual selector from app: input[name='name']
    @FindBy(css = "input[name='name'], input.form-control[placeholder*='Search']")
    WebElementFacade searchInput;

    // Parent filter dropdown - actual selector: select[name='parentId']
    @FindBy(css = "select[name='parentId'], select.form-select")
    WebElementFacade parentFilterDropdown;

    // Search button - actual selector: button.btn-primary (Search)
    @FindBy(css = "button.btn-primary")
    WebElementFacade searchButton;

    @FindBy(css = "a.btn.btn-outline-secondary, button.btn-outline-secondary")
    WebElementFacade resetButton;

    @FindBy(css = "a[href*='categories/add'], a.btn-primary[href*='add']")
    WebElementFacade addCategoryButton;

    @FindBy(css = "tbody tr")
    List<WebElementFacade> tableRows;

    @FindBy(css = "a.page-link")
    List<WebElementFacade> paginationLinks;

    @FindBy(css = "th a.text-white, th a.text-decoration-none")
    List<WebElementFacade> sortableHeaders;

    // ============== FORM ELEMENTS ==============

    @FindBy(css = "input.form-control[id='name'], input[name='name']")
    WebElementFacade categoryNameInput;

    @FindBy(css = "select.form-select[id='parentId'], select[name='parentId']")
    WebElementFacade parentCategoryDropdown;

    @FindBy(css = "button.btn-primary[type='submit'], button.btn-primary:not([href])")
    WebElementFacade saveButton;

    @FindBy(css = "a.btn-outline-secondary[href*='categories'], button.btn-outline-secondary")
    WebElementFacade cancelButton;

    // ============== ACTION BUTTONS ==============

    // Edit button - actual selector: a[title='Edit'] or a.btn-outline-primary
    @FindBy(css = "a[title='Edit'], a.btn-outline-primary[href*='edit']")
    List<WebElementFacade> editButtons;

    // Delete button - actual selector: button[title='Delete'] or
    // button.btn-outline-danger
    @FindBy(css = "button[title='Delete'], button.btn-outline-danger")
    List<WebElementFacade> deleteButtons;

    // ============== MESSAGES ==============

    @FindBy(css = ".invalid-feedback, .text-danger, .error-message")
    WebElementFacade validationError;

    @FindBy(css = ".alert-success, .toast-success, .success-message")
    WebElementFacade successMessage;

    @FindBy(css = ".alert-danger, .toast-danger, .error")
    WebElementFacade errorAlert;

    // ============== NAVIGATION METHODS ==============

    public void openCategoriesPage() {
        openUrl(Urls.UI_CATEGORIES);
    }

    public void openAddCategoryPage() {
        openUrl(Urls.UI_CATEGORIES_ADD);
    }

    public boolean isAt() {
        try {
            return title != null && title.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ============== LIST PAGE METHODS ==============

    public int getRowCount() {
        return tableRows.size();
    }

    public List<String> getCategoryNames() {
        return tableRows.stream()
                .map(row -> {
                    List<WebElement> cells = row.findElements(By.cssSelector("td"));
                    return cells.size() > 1 ? cells.get(1).getText() : "";
                })
                .collect(Collectors.toList());
    }

    public List<String> getCategoryIds() {
        return tableRows.stream()
                .map(row -> {
                    List<WebElement> cells = row.findElements(By.cssSelector("td"));
                    return cells.size() > 0 ? cells.get(0).getText() : "";
                })
                .collect(Collectors.toList());
    }

    public boolean hasNextPageLink() {
        return paginationLinks.stream()
                .anyMatch(link -> link.getText().contains("Next") || link.getText().equals("»"));
    }

    public void clickNextPage() {
        paginationLinks.stream()
                .filter(link -> link.getText().contains("Next") || link.getText().equals("»"))
                .findFirst()
                .ifPresent(link -> {
                    // Scroll element into view and use JavaScript click to avoid overlay issues
                    evaluateJavascript("arguments[0].scrollIntoView(true);", link);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ignored) {
                    }
                    evaluateJavascript("arguments[0].click();", link);
                });
    }

    public void clickSortByName() {
        sortableHeaders.stream()
                .filter(h -> h.getText().equalsIgnoreCase("Name"))
                .findFirst()
                .ifPresent(WebElementFacade::click);
    }

    public void clickSortById() {
        sortableHeaders.stream()
                .filter(h -> h.getText().equalsIgnoreCase("ID"))
                .findFirst()
                .ifPresent(WebElementFacade::click);
    }

    public void selectParentFilter(String parentName) {
        parentFilterDropdown.selectByVisibleText(parentName);
    }

    public void selectFirstParentFilter() {
        // Wait a bit for the page to be ready
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }

        // Select the first non-empty option
        try {
            List<WebElement> options = parentFilterDropdown.findElements(By.tagName("option"));
            if (options.size() > 1) {
                parentFilterDropdown.selectByIndex(1);
            }
        } catch (Exception e) {
            // Try alternative - find and select
        }
    }

    public void searchCategory(String searchTerm) {
        // Wait a bit for page to load
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }

        // Try to search
        try {
            if (searchInput.isVisible()) {
                searchInput.clear();
                searchInput.type(searchTerm);
                searchButton.click();
            }
        } catch (Exception e) {
            // Fallback: try to find search input by different selector
            try {
                WebElement input = getDriver().findElement(By.cssSelector("input[name='name']"));
                input.clear();
                input.sendKeys(searchTerm);
                WebElement btn = getDriver().findElement(By.cssSelector("button.btn-primary"));
                btn.click();
            } catch (Exception fallback) {
                // If still fails, log it
                throw new RuntimeException("Search input not found: " + fallback.getMessage());
            }
        }
    }

    public boolean isEmptyStateDisplayed() {
        // Strict check: requires a properly styled empty state UI element
        // Not just text or empty table - app should have a dedicated empty state
        // component
        try {
            // Look for a specific empty state element with proper styling
            WebElement emptyState = getDriver().findElement(
                    By.cssSelector(".empty-state, .no-data, [data-testid='empty-state']"));
            return emptyState.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ============== VISIBILITY CHECKS ==============

    public boolean isAddButtonVisible() {
        try {
            return addCategoryButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areEditButtonsVisible() {
        try {
            return !editButtons.isEmpty() && editButtons.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areDeleteButtonsVisible() {
        try {
            return !deleteButtons.isEmpty() && deleteButtons.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ============== FORM METHODS ==============

    public void clickAddCategory() {
        addCategoryButton.click();
    }

    public void enterCategoryName(String name) {
        categoryNameInput.clear();
        categoryNameInput.type(name);
    }

    public void clearCategoryName() {
        categoryNameInput.clear();
    }

    public void selectParentCategory(String parentName) {
        parentCategoryDropdown.selectByVisibleText(parentName);
    }

    public void selectFirstAvailableParent() {
        List<WebElement> options = parentCategoryDropdown.findElements(By.tagName("option"));
        // Skip "Main Category" or empty option, select actual parent
        for (int i = 1; i < options.size(); i++) {
            String text = options.get(i).getText();
            if (!text.isEmpty() && !text.equalsIgnoreCase("Main Category") && !text.contains("Select")) {
                parentCategoryDropdown.selectByIndex(i);
                break;
            }
        }
    }

    public void clickSave() {
        saveButton.click();
    }

    public void clickCancel() {
        try {
            // Try regular click first
            cancelButton.click();
        } catch (Exception e) {
            // Fallback: use JavaScript click or find by different selector
            try {
                WebElement btn = getDriver()
                        .findElement(By.cssSelector("a[href*='categories'].btn, button.btn-outline-secondary"));
                ((org.openqa.selenium.JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", btn);
            } catch (Exception fallback) {
                // Last resort: navigate back to categories page
                getDriver().navigate().to(Urls.UI_CATEGORIES);
            }
        }
    }

    public void clickFirstEditButton() {
        if (!editButtons.isEmpty()) {
            editButtons.get(0).click();
        }
    }

    public void clickFirstDeleteButton() {
        if (!deleteButtons.isEmpty()) {
            deleteButtons.get(0).click();
        }
    }

    public void clickDeleteForCategory(String categoryName) {
        for (WebElementFacade row : tableRows) {
            if (row.getText().contains(categoryName)) {
                WebElement deleteBtn = row.findElement(By.cssSelector("button.btn-outline-danger, button.btn-danger"));
                deleteBtn.click();
                break;
            }
        }
    }

    // ============== VALIDATION & MESSAGES ==============

    public boolean isValidationErrorDisplayed() {
        try {
            return validationError.isDisplayed();
        } catch (Exception e) {
            // Check page source for validation messages
            String source = getDriver().getPageSource().toLowerCase();
            return source.contains("required") ||
                    source.contains("must be") ||
                    source.contains("invalid") ||
                    source.contains("between");
        }
    }

    public String getValidationErrorText() {
        try {
            return validationError.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isSuccessMessageDisplayed() {
        try {
            return successMessage.isDisplayed();
        } catch (Exception e) {
            String source = getDriver().getPageSource().toLowerCase();
            return source.contains("success") || source.contains("created") || source.contains("updated");
        }
    }

    public boolean isErrorAlertDisplayed() {
        try {
            return errorAlert.isDisplayed();
        } catch (Exception e) {
            String source = getDriver().getPageSource().toLowerCase();
            return source.contains("error") || source.contains("cannot delete") || source.contains("has children");
        }
    }

    public boolean isDeleteBlockedMessageDisplayed() {
        String source = getDriver().getPageSource().toLowerCase();
        return source.contains("cannot delete") ||
                source.contains("has children") ||
                source.contains("has sub") ||
                source.contains("blocked");
    }

    // ============== HELPER METHODS ==============

    public String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }

    public boolean isOnCategoriesListPage() {
        String url = getCurrentUrl();
        return url.contains("/ui/categories") && !url.contains("/add") && !url.contains("/edit");
    }

    // ============== BACKWARD COMPATIBILITY METHODS ==============
    // These methods maintain compatibility with existing RolePagesUISteps

    public boolean addVisible() {
        return isAddButtonVisible();
    }

    public boolean editVisible() {
        return areEditButtonsVisible();
    }

    public boolean deleteVisible() {
        return areDeleteButtonsVisible();
    }
}
