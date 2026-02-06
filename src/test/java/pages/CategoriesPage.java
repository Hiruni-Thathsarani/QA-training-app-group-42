package pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.annotations.findby.FindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class CategoriesPage extends PageObject {

    @FindBy(css = "h1, h2, .page-title")
    WebElement title;

    @FindBy(css = "a[href*='categories/new'], button.add-category, .btn-add")
    WebElement addBtn;

    @FindBy(css = "button.edit, a[href*='edit'], .btn-edit")
    WebElement editBtn;

    @FindBy(css = "button.delete, .btn-delete")
    WebElement deleteBtn;

    private WebElement lastSearchInput;
    private WebElement lastParentSelect;

    public boolean isAt() {
        try { return title != null && title.isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public boolean addVisible() {
        try { return addBtn != null && addBtn.isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public boolean editVisible() {
        try { return editBtn != null && editBtn.isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public boolean deleteVisible() {
        try { return deleteBtn != null && deleteBtn.isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public boolean addEnabled() {
        return isEnabled(addBtn);
    }

    public boolean editEnabled() {
        return isEnabled(editBtn);
    }

    public boolean deleteEnabled() {
        return isEnabled(deleteBtn);
    }

    public boolean isListVisible() {
        return anyDisplayed("table, .category-list, .categories-list, .list-group, .data-table, tbody tr, .category-row, .list-item");
    }

    public void enterSearchTerm(String term) {
        WebElement input = firstDisplayed("input[type='search'], input[name*='search'], input[placeholder*='Search'], input[placeholder*='search'], input#search, input.search");
        if (input == null) return;
        lastSearchInput = input;
        try {
            input.clear();
            typeInto(input, term);
        } catch (Exception ignored) {}
    }

    public void applySearch() {
        WebElement button = firstDisplayed("button[type='submit'], button.search, .btn-search, button.apply, .btn-apply");
        if (button != null) {
            try { button.click(); } catch (Exception ignored) {}
            return;
        }
        if (lastSearchInput != null) {
            try { lastSearchInput.submit(); } catch (Exception ignored) {}
        }
    }

    public String selectFirstParentFilterOption() {
        WebElement selectEl = firstDisplayed("select[name*='parent'], select#parent, select.parent, .parent-filter select, select[name*='category']");
        if (selectEl == null) return null;
        lastParentSelect = selectEl;
        try {
            Select select = new Select(selectEl);
            List<WebElement> options = select.getOptions();
            for (WebElement option : options) {
                String value = option.getAttribute("value");
                if (value != null && !value.trim().isEmpty()) {
                    select.selectByValue(value);
                    return option.getText();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public String getSelectedParentText() {
        try {
            if (lastParentSelect == null) return null;
            return new Select(lastParentSelect).getFirstSelectedOption().getText();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean resultsContainTerm(String term) {
        try {
            String source = getDriver().getPageSource();
            return source != null && term != null && source.toLowerCase().contains(term.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    public String getFirstCategoryText() {
        WebElement cell = firstDisplayed("tbody tr td, .category-row, .list-group-item, .list-item");
        if (cell == null) return null;
        try {
            String text = cell.getText();
            return text != null && !text.trim().isEmpty() ? text.trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean hasErrorBanner() {
        return anyDisplayed(".alert-danger, .error, .invalid-feedback, .toast.error, .snackbar.error");
    }

    public void goToNextPage() {
        WebElement next = findNextPageElement();
        if (next != null) {
            try { next.click(); } catch (Exception ignored) {}
            return;
        }

        try {
            List<WebElement> links = getDriver().findElements(By.xpath("//a[contains(.,'Next') or contains(.,'›') or contains(.,'>')]"));
            for (WebElement link : links) {
                if (link.isDisplayed()) {
                    link.click();
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    public boolean hasNextPageControl() {
        return findNextPageElement() != null;
    }

    public boolean nextPageEnabled() {
        WebElement next = findNextPageElement();
        if (next == null) return false;
        try {
            if (!next.isDisplayed() || !next.isEnabled()) return false;
            String aria = next.getAttribute("aria-disabled");
            if (aria != null && aria.equalsIgnoreCase("true")) return false;
            String cls = next.getAttribute("class");
            if (cls != null && cls.toLowerCase().contains("disabled")) return false;
            WebElement parent = next.findElement(By.xpath("./.."));
            String parentCls = parent != null ? parent.getAttribute("class") : null;
            if (parentCls != null && parentCls.toLowerCase().contains("disabled")) return false;
        } catch (Exception ignored) {}
        return true;
    }

    public Integer getActivePageIndex() {
        WebElement active = firstDisplayed(".pagination .active, .page-item.active, li.active");
        if (active == null) return null;
        try {
            String text = active.getText().trim();
            return Integer.parseInt(text);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean anyDisplayed(String css) {
        List<WebElement> elements = getDriver().findElements(By.cssSelector(css));
        for (WebElement el : elements) {
            try {
                if (el != null && el.isDisplayed()) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    private WebElement firstDisplayed(String css) {
        List<WebElement> elements = getDriver().findElements(By.cssSelector(css));
        for (WebElement el : elements) {
            try {
                if (el != null && el.isDisplayed()) return el;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private WebElement findNextPageElement() {
        WebElement next = firstDisplayed("a[rel='next'], .pagination .next a, .pagination .page-item.next a, .pagination-next, a[aria-label*='Next'], button[aria-label*='Next']");
        if (next != null) return next;
        try {
            List<WebElement> links = getDriver().findElements(By.xpath("//a[contains(.,'Next') or contains(.,'›') or contains(.,'>')]"));
            for (WebElement link : links) {
                if (link.isDisplayed()) return link;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private boolean isEnabled(WebElement el) {
        try {
            return el != null && el.isDisplayed() && el.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }
}
