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

    @FindBy(css = "a[href*='plants/add'], a[href*='plants/new'], button.add-plant, .btn-add")
    WebElement addBtn;

    @FindBy(css = "button.edit, a[href*='edit'], .btn-edit")
    WebElement editBtn;

    @FindBy(css = "button.delete, .btn-delete")
    WebElement deleteBtn;

    @FindBy(css = "button[type='submit'], .btn-save, .btn-primary")
    WebElement saveBtn;

    private WebElement lastCategoryFilterSelect;

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
        return anyDisplayed(
                "table, .plants-list, .plant-list, .list-group, .data-table, tbody tr, .plant-row, .list-item");
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
                "a[href*='plants/add'], a[href*='plants/new'], a[href*='plants/create'], .btn-add, button.add-plant");
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

    public String selectFirstSubCategory() {
        WebElement selectEl = firstDisplayed(
                "select[name*='sub'], select[name*='category'], select#category, select.category, select#subCategory, select#categoryId, .category-select select");
        if (selectEl == null)
            return null;
        try {
            org.openqa.selenium.support.ui.Select select = new org.openqa.selenium.support.ui.Select(selectEl);
            List<WebElement> options = select.getOptions();
            for (WebElement option : options) {
                String value = option.getAttribute("value");
                String text = option.getText();
                if (value != null && !value.trim().isEmpty() && text != null && !text.trim().isEmpty()) {
                    select.selectByValue(value);
                    return option.getText();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public boolean selectCategoryByName(String name) {
        WebElement selectEl = firstDisplayed(
                "select[name*='sub'], select[name*='category'], select#category, select.category, select#subCategory, select#categoryId, .category-select select");
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
     * This fixes "plant should appear in list" flakiness.
     */
    public void savePlant() {
        if (saveBtn != null) {
            try {
                safeClick(saveBtn);
            } catch (Exception ignored) {
            }
        } else {
            WebElement btn = firstDisplayed("button[type='submit'], .btn-save, .btn-primary");
            if (btn != null) {
                try {
                    safeClick(btn);
                } catch (Exception ignored) {
                }
            }
        }

        // ✅ wait until redirected to /ui/plants and list is visible
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

    public String selectFirstCategoryFilterOption() {
        WebElement selectEl = firstDisplayed(
                "select[name*='category'], select#category, select.category, .category-filter select, select[name*='cat'], select#parent, select[name*='parent']");
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
                    return option.getText();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public String getSelectedCategoryFilterText() {
        try {
            if (lastCategoryFilterSelect == null)
                return null;
            return new org.openqa.selenium.support.ui.Select(lastCategoryFilterSelect).getFirstSelectedOption().getText();
        } catch (Exception e) {
            return null;
        }
    }

    public void applyFilterIfPresent() {
        WebElement btn = firstDisplayed(
                "button[type='submit'], button.search, .btn-search, .btn-apply, button.apply, button.btn-primary");
        if (btn != null) {
            try {
                safeClick(btn);
                return;
            } catch (Exception ignored) {
            }
        }
        WebElement btnByText = firstDisplayedXpath(
                "//button[contains(translate(normalize-space(.),'SEARCH','search'),'search')]");
        if (btnByText != null) {
            try {
                safeClick(btnByText);
                return;
            } catch (Exception ignored) {
            }
        }
        if (lastCategoryFilterSelect != null) {
            try {
                lastCategoryFilterSelect.submit();
            } catch (Exception ignored) {
            }
        }
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

    public boolean waitUntilPlantVisible(String name) {
        try {
            withTimeoutOf(Duration.ofSeconds(8)).waitForCondition()
                    .until(driver -> listContains(name));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------------- helpers ----------------

    private void waitForPlantsList() {
        withTimeoutOf(Duration.ofSeconds(8)).waitForCondition()
                .until(driver -> driver.getCurrentUrl().contains("/ui/plants"));

        withTimeoutOf(Duration.ofSeconds(8)).waitForCondition()
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

    private void safeClick(WebElement el) {
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
