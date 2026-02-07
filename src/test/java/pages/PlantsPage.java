package pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.annotations.findby.FindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;

public class PlantsPage extends PageObject {

    @FindBy(css = "h1, h2, .page-title")
    WebElement title;

    @FindBy(css = "a[href*='plants/add'], a[href*='plants/new'], button.add-plant, .btn-add, a.btn:contains('Add')")
    WebElement addBtn;

    @FindBy(css = "button.edit, a[href*='edit'], .btn-edit")
    WebElement editBtn;

    @FindBy(css = "button.delete, .btn-delete")
    WebElement deleteBtn;

    @FindBy(css = "button[type='submit'], .btn-save, .btn-primary")
    WebElement saveBtn;

    private WebElement lastCategoryFilterSelect;
    private String lastSelectedCategoryText;
    private String lastSelectedCategoryValue;

    public boolean isAt() {
        try {
            return title != null && title.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean adminActionsVisible() {
        try {
            return (addBtn != null && addBtn.isDisplayed())
                    || (editBtn != null && editBtn.isDisplayed())
                    || (deleteBtn != null && deleteBtn.isDisplayed());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isListVisible() {
        // Table should exist even if rows are filtered to 0
        return anyDisplayed("table") || anyDisplayed("thead") || anyDisplayed("tbody");
    }

    public void resetToPlantsList() {
        // Removes any ?categoryId=... or ?name=... filters
        openUrl(utils.Urls.UI_PLANTS);
        try {
            waitForPlantsList();
        } catch (Exception ignored) {
        }
    }

    public void openAddPlant() {
        if (addBtn != null) {
            try {
                addBtn.click();
                return;
            } catch (Exception ignored) {
            }
        }
        WebElement link = firstDisplayed(
                "a[href*='plants/add'], a[href*='plants/new'], a[href*='plants/create'], .btn-add, button.add-plant, a.btn.btn-primary");
        if (link != null) {
            try {
                link.click();
            } catch (Exception ignored) {
            }
        }
    }

    public void setPlantName(String name) {
        WebElement input = firstDisplayed(
                "input[name*='name'], input#name, input[placeholder*='Name'], input[placeholder*='name']");
        if (input == null)
            return;
        try {
            input.clear();
            typeInto(input, name);
        } catch (Exception ignored) {
        }
    }

    public void setPlantPriceIfPresent(String price) {
        WebElement input = firstDisplayed(
                "input[name*='price'], input#price, input[placeholder*='Price'], input[placeholder*='price']");
        if (input == null)
            return;
        try {
            input.clear();
            typeInto(input, price);
        } catch (Exception ignored) {
        }
    }

    public void setPlantQuantityIfPresent(String qty) {
        WebElement input = firstDisplayed("input[name*='quantity'], input[name*='stock'], input#quantity, input#stock");
        if (input == null)
            return;
        try {
            input.clear();
            typeInto(input, qty);
        } catch (Exception ignored) {
        }
    }

    public boolean selectCategoryByName(String name) {
        WebElement selectEl = firstDisplayed(
                "select[name*='sub'], select[name*='category'], select#categoryId, select#category, select.category, select.form-select, .category-select select");
        if (selectEl == null)
            return false;

        try {
            org.openqa.selenium.support.ui.Select select = new org.openqa.selenium.support.ui.Select(selectEl);
            for (WebElement option : select.getOptions()) {
                String text = option.getText();
                if (text != null && text.contains(name)) {
                    select.selectByVisibleText(option.getText());
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Click Save and wait until we are back on the plants list and it has rendered.
     */
    public void savePlant() {
        WebElement btn = saveBtn != null ? saveBtn : firstDisplayed("button[type='submit'], .btn-save, .btn-primary");
        if (btn != null) {
            try {
                safeClick(btn);
            } catch (Exception ignored) {
            }
        }

        // Wait until redirected back to plants list
        try {
            waitForPlantsList();
        } catch (Exception ignored) {
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

    // ----------- FILTER (USER SCENARIO) -----------

    public String selectFirstCategoryFilterOption() {
        WebElement selectEl = firstDisplayed(
                "select[name='categoryId'], select#categoryId, .plants-filters select, select.form-select, select[name*='category']");
        if (selectEl == null)
            return null;

        lastCategoryFilterSelect = selectEl;

        try {
            org.openqa.selenium.support.ui.Select select = new org.openqa.selenium.support.ui.Select(selectEl);
            List<WebElement> options = select.getOptions();
            for (WebElement option : options) {
                String value = option.getAttribute("value");
                String text = option.getText();
                if (value != null && !value.trim().isEmpty() && text != null && !text.trim().isEmpty()) {
                    select.selectByValue(value);
                    lastSelectedCategoryText = text.trim();
                    lastSelectedCategoryValue = value.trim();
                    return lastSelectedCategoryText;
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    public String getSelectedCategoryFilterText() {
        if (lastSelectedCategoryText != null)
            return lastSelectedCategoryText;

        // re-find to avoid stale element
        WebElement selectEl = firstDisplayed(
                "select[name='categoryId'], select#categoryId, .plants-filters select, select.form-select, select[name*='category']");
        if (selectEl == null)
            return null;

        try {
            return new org.openqa.selenium.support.ui.Select(selectEl).getFirstSelectedOption().getText().trim();
        } catch (Exception e) {
            return null;
        }
    }

    public void applyFilterIfPresent() {
        // Click the button that actually says Search (your UI has a Search button)
        WebElement searchBtn = firstDisplayedXpath(
                "//button[normalize-space()='Search' or contains(translate(normalize-space(.),'SEARCH','search'),'search')]");
        if (searchBtn != null) {
            try {
                safeClick(searchBtn);
            } catch (Exception ignored) {
            }
        }

        // Wait for list to re-render (URL contains /ui/plants and table exists)
        try {
            waitForPlantsList();
        } catch (Exception ignored) {
        }

        // Ensure category filter has been applied in query string
        if (lastSelectedCategoryValue != null && !lastSelectedCategoryValue.isBlank()) {
            try {
                withTimeoutOf(Duration.ofSeconds(10)).waitForCondition()
                        .until(driver -> driver.getCurrentUrl().contains("categoryId=" + lastSelectedCategoryValue));
            } catch (Exception ignored) {
            }
        }
    }

    private int getColumnIndexByHeader(String... headerNames) {
        List<WebElement> headers = getDriver().findElements(By.cssSelector("table thead th"));
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).getText();
            if (h == null)
                continue;
            String norm = h.trim().toLowerCase();
            for (String name : headerNames) {
                String n = name.toLowerCase();
                if (norm.equals(n) || norm.contains(n)) {
                    return i + 1; // nth-child is 1-based
                }
            }
        }
        return -1;
    }

    public String getFirstListedPlantCategory() {
        int col = getColumnIndexByHeader("category", "sub category", "subcategory");
        if (col == -1)
            return null;

        WebElement cell = firstDisplayed("tbody tr:first-child td:nth-child(" + col + ")");
        if (cell == null)
            return null;

        String text = cell.getText();
        return (text == null || text.trim().isEmpty()) ? null : text.trim();
    }

    public List<String> getVisibleListedPlantCategories() {
        List<String> categories = new java.util.ArrayList<>();

        int col = getColumnIndexByHeader("category", "sub category", "subcategory");
        if (col == -1)
            return categories;

        List<WebElement> cells = getDriver().findElements(By.cssSelector("tbody tr td:nth-child(" + col + ")"));
        for (WebElement cell : cells) {
            if (cell == null)
                continue;
            String text = cell.getText();
            if (text != null && !text.trim().isEmpty()) {
                categories.add(text.trim());
            }
        }
        return categories;
    }

    public void enterListSearchTerm(String term) {
        WebElement input = firstDisplayed(
                "input[placeholder*='Search plant'], input[placeholder*='Search'], input[name='name'], input[name*='name']");
        if (input == null) {
            return;
        }
        try {
            input.clear();
            typeInto(input, term);
        } catch (Exception ignored) {
        }
    }

    public String getFirstListedPlantName() {
        try {
            WebElement cell = firstDisplayed("tbody tr:first-child td:nth-child(1)");
            if (cell == null) {
                return null;
            }
            String text = cell.getText();
            return (text == null || text.trim().isEmpty()) ? null : text.trim();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean waitUntilPlantVisible(String name) {
        try {
            withTimeoutOf(Duration.ofSeconds(10)).waitForCondition()
                    .until(driver -> listContains(name));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ----------- Helpers -----------

    private void waitForPlantsList() {
        withTimeoutOf(Duration.ofSeconds(10)).waitForCondition()
                .until(driver -> driver.getCurrentUrl().contains("/ui/plants"));

        withTimeoutOf(Duration.ofSeconds(10)).waitForCondition()
                .until(driver -> isListVisible());
    }

    private boolean anyDisplayed(String css) {
        List<WebElement> elements = getDriver().findElements(By.cssSelector(css));
        for (WebElement el : elements) {
            try {
                if (el != null && el.isDisplayed())
                    return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private WebElement firstDisplayed(String css) {
        List<WebElement> elements = getDriver().findElements(By.cssSelector(css));
        for (WebElement el : elements) {
            try {
                if (el != null && el.isDisplayed())
                    return el;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private WebElement firstDisplayedXpath(String xpath) {
        try {
            List<WebElement> elements = getDriver().findElements(By.xpath(xpath));
            for (WebElement el : elements) {
                if (el != null && el.isDisplayed())
                    return el;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void safeClick(WebElement el) {
        if (el == null)
            return;
        try {
            waitFor(el).waitUntilClickable().click();
        } catch (Exception e) {
            try {
                el.click();
            } catch (Exception ignored) {
            }
        }
    }
}
