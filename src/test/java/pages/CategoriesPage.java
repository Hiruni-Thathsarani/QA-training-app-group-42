package pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.annotations.findby.FindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;

public class CategoriesPage extends PageObject {

    @FindBy(css = "h1, h2, .page-title")
    WebElement title;

    @FindBy(css = "a[href*='categories/add'], a[href*='categories/new'], button.add-category, .btn-add")
    WebElement addBtn;

    @FindBy(css = "button.edit, a[href*='edit'], .btn-edit")
    WebElement editBtn;

    @FindBy(css = "button.delete, .btn-delete")
    WebElement deleteBtn;

    @FindBy(css = "button[type='submit'], .btn-save, .btn-primary")
    WebElement saveBtn;

    private WebElement lastSearchInput;
    private WebElement lastParentSelect;

    public boolean isAt() {
        try {
            return title != null && title.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean addVisible() {
        try {
            return addBtn != null && addBtn.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean editVisible() {
        try {
            return editBtn != null && editBtn.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteVisible() {
        try {
            return deleteBtn != null && deleteBtn.isDisplayed();
        } catch (Exception e) {
            return false;
        }
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
        return anyDisplayed(
                "table, .category-list, .categories-list, .list-group, .data-table, tbody tr, .category-row, .list-item");
    }

    public void openAddCategory() {
        if (addBtn != null) {
            try {
                addBtn.click();
                return;
            } catch (Exception ignored) {
            }
        }
        WebElement link = firstDisplayed(
                "a[href*='categories/add'], a[href*='categories/new'], a[href*='categories/create'], .btn-add, button.add-category");
        if (link != null) {
            try {
                link.click();
            } catch (Exception ignored) {
            }
        }
    }

    public void setCategoryName(String name) {
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

    public void selectNoParentIfPossible() {
        WebElement selectEl = firstDisplayed(
                "select[name*='parent'], select#parent, select.parent, .parent-filter select, select[name*='category']");
        if (selectEl == null)
            return;
        try {
            Select select = new Select(selectEl);
            List<WebElement> options = select.getOptions();
            for (WebElement option : options) {
                String value = option.getAttribute("value");
                String text = option.getText();
                if (value == null)
                    value = "";
                String v = value.trim().toLowerCase();
                String t = text == null ? "" : text.trim().toLowerCase();
                if (v.isEmpty() || v.equals("0") || t.contains("none") || t.contains("no parent")) {
                    select.selectByVisibleText(option.getText());
                    return;
                }
            }
        } catch (Exception ignored) {
        }
    }

    public String selectFirstParentOption() {
        WebElement selectEl = firstDisplayed(
                "select[name*='parent'], select#parent, select.parent, .parent-filter select, select[name*='category']");
        if (selectEl == null)
            return null;
        try {
            Select select = new Select(selectEl);
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

    public boolean selectParentByName(String name) {
        WebElement selectEl = firstDisplayed(
                "select[name*='parent'], select#parent, select.parent, .parent-filter select, select[name*='category']");
        if (selectEl == null)
            return false;
        try {
            Select select = new Select(selectEl);
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
     * Click Save and wait until we are back on the Categories list and it has
     * rendered.
     * This removes flakiness where subsequent steps run while still on the form
     * page.
     */
    public void saveCategory() {
        if (saveBtn != null) {
            try {
                saveBtn.click();
            } catch (Exception ignored) {
            }
        } else {
            WebElement btn = firstDisplayed("button[type='submit'], .btn-save, .btn-primary");
            if (btn != null) {
                try {
                    btn.click();
                } catch (Exception ignored) {
                }
            }
        }


        try {
            waitForCategoriesList();
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

    /**
     * Wait until a row containing the given name is visible (best-effort, avoids
     * immediate false).
     */
    public boolean waitUntilRowVisible(String name) {
        try {
            withTimeoutOf(Duration.ofSeconds(6)).waitForCondition()
                    .until(driver -> listContains(name));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean openEditForName(String name) {
        try {
            // Ensure list is ready before scanning rows
            waitForCategoriesList();
        } catch (Exception ignored) {
        }

        try {
            List<WebElement> rows = getDriver()
                    .findElements(By.cssSelector("tbody tr, .category-row, .list-group-item, .list-item"));
            for (WebElement row : rows) {
                if (row == null)
                    continue;
                String text = row.getText();
                if (text != null && text.contains(name)) {
                    WebElement edit = firstInRow(row,
                            "a[href*='categories/edit'], a[href*='/edit/'], button.edit, a[href*='edit'], .btn-edit, button[aria-label*='Edit']");
                    if (edit != null) {
                        edit.click();
                        return true;
                    }

                    WebElement editByText = firstInRowXpath(row,
                            ".//*[self::a or self::button][contains(translate(normalize-space(.),'EDIT','edit'),'edit')]");
                    if (editByText != null) {
                        editByText.click();
                        return true;
                    }

                    String editHref = firstRowLinkHref(row, "edit");
                    if (editHref != null) {
                        openUrl(editHref);
                        return true;
                    }

                    List<WebElement> links = row.findElements(By.cssSelector("a[href]"));
                    for (WebElement link : links) {
                        String linkHref = link.getAttribute("href");
                        if (linkHref != null && (linkHref.contains("/edit/") || linkHref.contains("categories/edit"))) {
                            link.click();
                            return true;
                        }
                    }

                    String id = getRowId(row);
                    if (id != null) {
                        openUrl(utils.Urls.UI_CATEGORIES + "/edit/" + id);
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public boolean openEditForFirstRow() {
        WebElement edit = firstDisplayed(
                "tbody tr a[href*='categories/edit'], tbody tr a[href*='/edit/'], tbody tr button.edit, tbody tr a[href*='edit'], .btn-edit, button.edit");
        if (edit == null)
            return false;
        try {
            edit.click();
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    public boolean deleteByName(String name) {
        try {
            // Ensure list is ready before scanning rows
            waitForCategoriesList();
        } catch (Exception ignored) {
        }

        try {
            List<WebElement> rows = getDriver()
                    .findElements(By.cssSelector("tbody tr, .category-row, .list-group-item, .list-item"));
            for (WebElement row : rows) {
                if (row == null)
                    continue;
                String text = row.getText();
                if (text != null && text.contains(name)) {
                    WebElement del = firstInRow(row,
                            "a[href*='categories/delete'], button.delete, .btn-delete, button[aria-label*='Delete'], .btn-danger, .btn-outline-danger, a[class*='danger'], button[class*='danger']");
                    if (del == null) {
                        boolean menuOpened = clickRowActionMenu(row);
                        if (menuOpened) {
                            del = firstInRow(row,
                                    "a[href*='categories/delete'], button.delete, .btn-delete, button[aria-label*='Delete'], .btn-danger, .btn-outline-danger, a[class*='danger'], button[class*='danger']");
                        }
                    }
                    if (del != null) {
                        del.click();
                        confirmDeleteIfNeeded();
                        try {
                            waitForCategoriesList();
                        } catch (Exception ignored2) {
                        }
                        return true;
                    }

                    WebElement delByText = firstInRowXpath(row,
                            ".//*[self::a or self::button][contains(translate(normalize-space(.),'DELETE','delete'),'delete')]");
                    if (delByText == null) {
                        boolean menuOpened = clickRowActionMenu(row);
                        if (menuOpened) {
                            delByText = firstInRowXpath(row,
                                    ".//*[self::a or self::button][contains(translate(normalize-space(.),'DELETE','delete'),'delete')]");
                        }
                    }
                    if (delByText != null) {
                        delByText.click();
                        confirmDeleteIfNeeded();
                        try {
                            waitForCategoriesList();
                        } catch (Exception ignored2) {
                        }
                        return true;
                    }

                    WebElement delByIcon = findTrashActionInRow(row);
                    if (delByIcon == null) {
                        boolean menuOpened = clickRowActionMenu(row);
                        if (menuOpened) {
                            delByIcon = findTrashActionInRow(row);
                        }
                    }
                    if (delByIcon != null) {
                        delByIcon.click();
                        confirmDeleteIfNeeded();
                        try {
                            waitForCategoriesList();
                        } catch (Exception ignored2) {
                        }
                        return true;
                    }

                    WebElement delByAttrs = findDeleteActionInRow(row);
                    if (delByAttrs == null) {
                        boolean menuOpened = clickRowActionMenu(row);
                        if (menuOpened) {
                            delByAttrs = findDeleteActionInRow(row);
                        }
                    }
                    if (delByAttrs != null) {
                        delByAttrs.click();
                        confirmDeleteIfNeeded();
                        try {
                            waitForCategoriesList();
                        } catch (Exception ignored2) {
                        }
                        return true;
                    }

                    String deleteHref = firstRowLinkHref(row, "delete");
                    if (deleteHref != null) {
                        openUrl(deleteHref);
                        confirmDeleteIfNeeded();
                        try {
                            waitForCategoriesList();
                        } catch (Exception ignored2) {
                        }
                        return true;
                    }

                    List<WebElement> links = row.findElements(By.cssSelector("a[href]"));
                    for (WebElement link : links) {
                        String linkHref = link.getAttribute("href");
                        if (linkHref != null
                                && (linkHref.contains("/delete/") || linkHref.contains("categories/delete"))) {
                            link.click();
                            confirmDeleteIfNeeded();
                            try {
                                waitForCategoriesList();
                            } catch (Exception ignored2) {
                            }
                            return true;
                        }
                    }

                    String id = getRowId(row);
                    if (id != null) {
                        openUrl(utils.Urls.UI_CATEGORIES + "/delete/" + id);
                        confirmDeleteIfNeeded();
                        try {
                            waitForCategoriesList();
                        } catch (Exception ignored2) {
                        }
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private String getRowId(WebElement row) {
        String id = row.getAttribute("data-id");
        if (id == null || id.isBlank())
            id = row.getAttribute("data-category-id");
        if (id == null || id.isBlank())
            id = row.getAttribute("data-cat-id");
        if (id == null || id.isBlank())
            id = row.getAttribute("id");
        return (id == null || id.isBlank()) ? null : id.trim();
    }

    private WebElement firstInRow(WebElement row, String css) {
        try {
            List<WebElement> elements = row.findElements(By.cssSelector(css));
            for (WebElement el : elements) {
                if (el != null && el.isDisplayed())
                    return el;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String firstRowLinkHref(WebElement row, String keyword) {
        try {
            List<WebElement> links = row.findElements(By.cssSelector("a[href]"));
            for (WebElement link : links) {
                String href = link.getAttribute("href");
                if (href != null && href.toLowerCase().contains(keyword))
                    return href;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private WebElement firstInRowXpath(WebElement row, String xpath) {
        try {
            List<WebElement> elements = row.findElements(By.xpath(xpath));
            for (WebElement el : elements) {
                if (el != null && el.isDisplayed())
                    return el;
            }
        } catch (Exception ignored) {
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

    private boolean clickRowActionMenu(WebElement row) {
        try {
            WebElement menu = firstInRow(row,
                    "button[aria-label*='Action'], button[aria-label*='More'], .dropdown-toggle, button.actions, .btn-actions, .btn-more");
            if (menu == null) {
                menu = firstInRowXpath(row,
                        ".//*[self::button or self::a][contains(translate(normalize-space(.),'ACTIONS','actions'),'actions') or contains(translate(normalize-space(.),'MORE','more'),'more') or contains(translate(normalize-space(.),'MENU','menu'),'menu')]");
            }
            if (menu != null) {
                menu.click();
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private WebElement findTrashActionInRow(WebElement row) {
        return firstInRowXpath(row,
                ".//*[self::button or self::a][.//*[self::svg or self::i][contains(@data-icon,'trash') or contains(@class,'trash') or contains(@class,'fa-trash') or contains(@class,'bi-trash')]]");
    }

    private WebElement findDeleteActionInRow(WebElement row) {
        try {
            List<WebElement> elements = row.findElements(By.cssSelector("td:last-child button, td:last-child a"));
            for (WebElement el : elements) {
                if (el != null && el.isDisplayed() && isLikelyDeleteAction(el))
                    return el;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isLikelyDeleteAction(WebElement el) {
        try {
            String cls = el.getAttribute("class");
            String aria = el.getAttribute("aria-label");
            String title = el.getAttribute("title");
            String dataTitle = el.getAttribute("data-bs-original-title");
            String text = el.getText();
            String combined = (cls + " " + aria + " " + title + " " + dataTitle + " " + text).toLowerCase();
            return combined.contains("delete") || combined.contains("trash") || combined.contains("danger");
        } catch (Exception e) {
            return false;
        }
    }

    private void confirmDeleteIfNeeded() {

        // Wait for JS alert and accept
        try {
            withTimeoutOf(java.time.Duration.ofSeconds(5)).waitForCondition().until(driver -> {
                try {
                    driver.switchTo().alert();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            });

            getDriver().switchTo().alert().accept();
            return;
        } catch (Exception ignored) {
        }

        // fallback UI modal
        WebElement confirm = firstDisplayed(
                "button.confirm, .btn-confirm, .modal-footer .btn-danger, .swal2-confirm");
        if (confirm != null) {
            safeClick(confirm);
        }
    }

    public boolean deleteFromEditPage() {
        WebElement del = firstDisplayed(
                "a[href*='delete'], button.delete, .btn-delete, .btn-danger, .btn-outline-danger, button[aria-label*='Delete'], form[action*='delete'] button, form[action*='delete'] input[type='submit'], a[class*='danger'], button[class*='danger']");
        if (del == null) {
            WebElement menu = firstDisplayed(
                    "button[aria-label*='Action'], button[aria-label*='More'], .dropdown-toggle, button.actions, .btn-actions, .btn-more");
            if (menu != null) {
                try {
                    menu.click();
                } catch (Exception ignored) {
                }
            }
            del = firstDisplayed(
                    "a[href*='delete'], button.delete, .btn-delete, .btn-danger, .btn-outline-danger, button[aria-label*='Delete'], form[action*='delete'] button, form[action*='delete'] input[type='submit'], a[class*='danger'], button[class*='danger']");
        }
        if (del == null) {
            del = firstDisplayedXpath(
                    "//*[self::a or self::button or self::input][contains(translate(normalize-space(.),'DELETE','delete'),'delete')]");
        }
        if (del == null) {
            del = firstDisplayedXpath(
                    "//*[self::button or self::a][.//*[self::svg or self::i][contains(@data-icon,'trash') or contains(@class,'trash') or contains(@class,'fa-trash') or contains(@class,'bi-trash')]]");
        }
        if (del == null)
            return false;
        try {
            del.click();
            confirmDeleteIfNeeded();
            try {
                waitForCategoriesList();
            } catch (Exception ignored2) {
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public void enterSearchTerm(String term) {
        WebElement input = firstDisplayed(
                "input[type='search'], input[name*='search'], input[placeholder*='Search'], input[placeholder*='search'], input#search, input.search");
        if (input == null)
            return;
        lastSearchInput = input;
        try {
            input.clear();
            typeInto(input, term);
        } catch (Exception ignored) {
        }
    }

    /**
     * Submit the correct search form instead of clicking a generic submit button
     * (which might be Save).
     */
    public void applySearch() {
        // Prefer submitting the form that owns the search input
        if (lastSearchInput != null) {
            try {
                WebElement form = lastSearchInput.findElement(By.xpath("./ancestor::form"));
                if (form != null) {
                    form.submit();
                    try {
                        waitForCategoriesList();
                    } catch (Exception ignored) {
                    }
                    return;
                }
            } catch (Exception ignored) {
            }

            try {
                lastSearchInput.submit();
                try {
                    waitForCategoriesList();
                } catch (Exception ignored) {
                }
                return;
            } catch (Exception ignored) {
            }
        }

        // Fallback: only click explicit search/apply buttons (NO generic submit)
        WebElement button = firstDisplayed("button.search, .btn-search, button.apply, .btn-apply");
        if (button != null) {
            try {
                button.click();
            } catch (Exception ignored) {
            }
        }

        try {
            waitForCategoriesList();
        } catch (Exception ignored) {
        }
    }

    public String selectFirstParentFilterOption() {
        WebElement selectEl = firstDisplayed(
                "select[name*='parent'], select#parent, select.parent, .parent-filter select, select[name*='category']");
        if (selectEl == null)
            return null;
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
        } catch (Exception ignored) {
        }
        return null;
    }

    public String getSelectedParentText() {
        try {
            if (lastParentSelect == null)
                return null;
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
        if (cell == null)
            return null;
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
            try {
                next.click();
            } catch (Exception ignored) {
            }
            return;
        }

        try {
            List<WebElement> links = getDriver()
                    .findElements(By.xpath("//a[contains(.,'Next') or contains(.,'›') or contains(.,'>')]"));
            for (WebElement link : links) {
                if (link.isDisplayed()) {
                    link.click();
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }

    public boolean hasNextPageControl() {
        return findNextPageElement() != null;
    }

    public boolean nextPageEnabled() {
        WebElement next = findNextPageElement();
        if (next == null)
            return false;
        try {
            if (!next.isDisplayed() || !next.isEnabled())
                return false;
            String aria = next.getAttribute("aria-disabled");
            if (aria != null && aria.equalsIgnoreCase("true"))
                return false;
            String cls = next.getAttribute("class");
            if (cls != null && cls.toLowerCase().contains("disabled"))
                return false;
            WebElement parent = next.findElement(By.xpath("./.."));
            String parentCls = parent != null ? parent.getAttribute("class") : null;
            if (parentCls != null && parentCls.toLowerCase().contains("disabled"))
                return false;
        } catch (Exception ignored) {
        }
        return true;
    }

    public Integer getActivePageIndex() {
        WebElement active = firstDisplayed(".pagination .active, .page-item.active, li.active");
        if (active == null)
            return null;
        try {
            String text = active.getText().trim();
            return Integer.parseInt(text);
        } catch (Exception e) {
            return null;
        }
    }

    // ===================== New internal wait helper =====================

    /**
     * Wait until we're on /ui/categories and the list has rendered at least one
     * row/container.
     * Prevents false negatives when actions are queried too early.
     */
    private void waitForCategoriesList() {
        withTimeoutOf(Duration.ofSeconds(8)).waitForCondition()
                .until(driver -> driver.getCurrentUrl().contains("/ui/categories"));

        // "Rendered" check: any of these indicates list is present.
        withTimeoutOf(Duration.ofSeconds(8)).waitForCondition()
                .until(driver -> anyDisplayed(
                        "tbody tr, .category-row, .list-group-item, .list-item, table, .category-list, .categories-list"));
    }

    // ===================== Existing helpers =====================

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

    private WebElement findNextPageElement() {
        WebElement next = firstDisplayed(
                "a[rel='next'], .pagination .next a, .pagination .page-item.next a, .pagination-next, a[aria-label*='Next'], button[aria-label*='Next']");
        if (next != null)
            return next;
        try {
            List<WebElement> links = getDriver()
                    .findElements(By.xpath("//a[contains(.,'Next') or contains(.,'›') or contains(.,'>')]"));
            for (WebElement link : links) {
                if (link.isDisplayed())
                    return link;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isEnabled(WebElement el) {
        try {
            return el != null && el.isDisplayed() && el.isEnabled();
        } catch (Exception e) {
            return false;
        }
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
