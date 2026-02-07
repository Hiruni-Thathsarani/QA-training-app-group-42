// CategoriesPage.java
package pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import utils.Urls;

import java.util.List;
import java.util.stream.Collectors;

public class CategoriesPage extends PageObject {

    @FindBy(css = "h1, h2, .page-title")
    WebElementFacade title;

    @FindBy(css = "input[name='name'], input.form-control[placeholder*='Search']")
    WebElementFacade searchInput;

    @FindBy(css = "select[name='parentId'], select.form-select")
    WebElementFacade parentFilterDropdown;

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

    @FindBy(css = "input.form-control[id='name'], input[name='name']")
    WebElementFacade categoryNameInput;

    @FindBy(css = "select.form-select[id='parentId'], select[name='parentId']")
    WebElementFacade parentCategoryDropdown;

    @FindBy(css = "button.btn-primary[type='submit'], button.btn-primary:not([href])")
    WebElementFacade saveButton;

    @FindBy(css = "a.btn-outline-secondary[href*='categories'], button.btn-outline-secondary")
    WebElementFacade cancelButton;

    @FindBy(css = "a[title='Edit'], a.btn-outline-primary[href*='edit']")
    List<WebElementFacade> editButtons;

    @FindBy(css = "button[title='Delete'], button.btn-outline-danger")
    List<WebElementFacade> deleteButtons;

    @FindBy(css = ".invalid-feedback, .text-danger, .error-message")
    WebElementFacade validationError;

    @FindBy(css = ".alert-success, .toast-success, .success-message")
    WebElementFacade successMessage;

    @FindBy(css = ".alert-danger, .toast-danger, .error")
    WebElementFacade errorAlert;

    public void openCategoriesPage() {
        openUrl(Urls.UI_CATEGORIES);
    }

    public void openAddCategoryPage() {
        openUrl(Urls.UI_CATEGORIES_ADD);
    }

    private WebElement lastSearchInput;
    private WebElement lastParentSelect;

    public boolean isAt() {
        try {
            return title != null && title.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

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
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }

        try {
            List<WebElement> options = parentFilterDropdown.findElements(By.tagName("option"));
            if (options.size() > 1) {
                parentFilterDropdown.selectByIndex(1);
            }
        } catch (Exception ignored) {
        }
    }

    public void searchCategory(String searchTerm) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }

        try {
            if (searchInput.isVisible()) {
                searchInput.clear();
                searchInput.type(searchTerm);
                searchButton.click();
            }
        } catch (Exception e) {
            try {
                WebElement input = getDriver().findElement(By.cssSelector("input[name='name']"));
                input.clear();
                input.sendKeys(searchTerm);
                WebElement btn = getDriver().findElement(By.cssSelector("button.btn-primary"));
                btn.click();
            } catch (Exception fallback) {
                throw new RuntimeException("Search input not found: " + fallback.getMessage());
            }
        }
    }

    public boolean isEmptyStateDisplayed() {
        try {
            WebElement emptyState = getDriver().findElement(
                    By.cssSelector(".empty-state, .no-data, [data-testid='empty-state']"));
            return emptyState.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

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
            cancelButton.click();
        } catch (Exception e) {
            try {
                WebElement btn = getDriver()
                        .findElement(By.cssSelector("a[href*='categories'].btn, button.btn-outline-secondary"));
                ((org.openqa.selenium.JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", btn);
            } catch (Exception fallback) {
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

    public boolean isValidationErrorDisplayed() {
        try {
            return validationError.isDisplayed();
        } catch (Exception e) {
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

    public String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }

    public boolean isOnCategoriesListPage() {
        String url = getCurrentUrl();
        return url.contains("/ui/categories") && !url.contains("/add") && !url.contains("/edit");
    }

    public boolean addVisible() {
        return isAddButtonVisible();
    }

    public boolean editVisible() {
        return areEditButtonsVisible();
    }

    public boolean deleteVisible() {
        return areDeleteButtonsVisible();
    }

    public boolean isListVisible() {
        try {
            return !tableRows.isEmpty() || getDriver().findElements(By.cssSelector("table, tbody tr")).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean listContains(String text) {
        try {
            String source = getDriver().getPageSource();
            return source != null && text != null && source.contains(text);
        } catch (Exception e) {
            return false;
        }
    }

    public void openAddCategory() {
        try {
            if (addCategoryButton != null) {
                addCategoryButton.click();
            }
        } catch (Exception ignored) {
        }
    }

    public void setCategoryName(String name) {
        enterCategoryName(name);
    }

    public void selectNoParentIfPossible() {
        try {
            List<WebElement> options = parentCategoryDropdown.findElements(By.tagName("option"));
            for (WebElement option : options) {
                String value = option.getAttribute("value");
                String text = option.getText();
                if (value == null) value = "";
                String v = value.trim().toLowerCase();
                String t = text == null ? "" : text.trim().toLowerCase();
                if (v.isEmpty() || v.equals("0") || t.contains("none") || t.contains("no parent") || t.contains("main")) {
                    parentCategoryDropdown.selectByVisibleText(option.getText());
                    return;
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void saveCategory() {
        clickSave();
    }

    public void enterSearchTerm(String term) {
        try {
            if (searchInput != null) {
                lastSearchInput = searchInput;
                searchInput.clear();
                searchInput.type(term);
            }
        } catch (Exception ignored) {
        }
    }

    public void applySearch() {
        try {
            if (searchButton != null) {
                searchButton.click();
            }
        } catch (Exception ignored) {
        }
    }

    public boolean openEditForName(String name) {
        try {
            for (WebElementFacade row : tableRows) {
                if (row.getText().contains(name)) {
                    WebElement edit = row.findElement(By.cssSelector("a[title='Edit'], a[href*='edit'], .btn-outline-primary"));
                    edit.click();
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public boolean deleteByName(String name) {
        try {
            clickDeleteForCategory(name);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteFromEditPage() {
        try {
            WebElement del = getDriver().findElement(By.cssSelector("a[href*='delete'], button.btn-outline-danger, button.btn-danger"));
            del.click();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getFirstCategoryText() {
        try {
            if (!tableRows.isEmpty()) {
                List<WebElement> cells = tableRows.get(0).findElements(By.cssSelector("td"));
                if (!cells.isEmpty()) return cells.get(0).getText();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public boolean resultsContainTerm(String term) {
        try {
            String source = getDriver().getPageSource();
            return source != null && term != null && source.toLowerCase().contains(term.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasErrorBanner() {
        try {
            return errorAlert != null && errorAlert.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String selectFirstParentFilterOption() {
        try {
            List<WebElement> options = parentFilterDropdown.findElements(By.tagName("option"));
            for (WebElement option : options) {
                String value = option.getAttribute("value");
                if (value != null && !value.trim().isEmpty()) {
                    parentFilterDropdown.selectByValue(value);
                    lastParentSelect = parentFilterDropdown;
                    return option.getText();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public String getSelectedParentText() {
        try {
            if (lastParentSelect == null) lastParentSelect = parentFilterDropdown;
            return new org.openqa.selenium.support.ui.Select(lastParentSelect)
                    .getFirstSelectedOption()
                    .getText();
        } catch (Exception e) {
            return null;
        }
    }


    public Integer getActivePageIndex() {
        try {
            WebElement active = getDriver().findElement(By.cssSelector(".pagination .active, .page-item.active, li.active"));
            String text = active.getText().trim();
            return Integer.parseInt(text);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean nextPageEnabled() {
        try {
            return hasNextPageLink();
        } catch (Exception e) {
            return false;
        }
    }

    public void goToNextPage() {
        clickNextPage();
    }

    public boolean addEnabled() {
        try {
            return addCategoryButton != null && addCategoryButton.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean editEnabled() {
        try {
            return !editButtons.isEmpty() && editButtons.get(0).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteEnabled() {
        try {
            return !deleteButtons.isEmpty() && deleteButtons.get(0).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public String selectFirstParentOption() {
        try {
            List<WebElement> options = parentCategoryDropdown.findElements(By.tagName("option"));
            for (int i = 1; i < options.size(); i++) {
                String value = options.get(i).getAttribute("value");
                String text = options.get(i).getText();
                if (value != null && !value.trim().isEmpty() && text != null && !text.trim().isEmpty()) {
                    parentCategoryDropdown.selectByValue(value);
                    return text;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public boolean selectParentByName(String name) {
        try {
            List<WebElement> options = parentCategoryDropdown.findElements(By.tagName("option"));
            for (WebElement option : options) {
                String text = option.getText();
                if (text != null && text.contains(name)) {
                    parentCategoryDropdown.selectByVisibleText(option.getText());
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
