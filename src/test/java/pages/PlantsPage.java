package pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.annotations.findby.FindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlantsPage extends PageObject {

    @FindBy(css = "h1, h2, .page-title")
    WebElement title;

    @FindBy(css = "a[href*='plants/new'], button.add-plant, .btn-add")
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
        try { return title != null && title.isDisplayed(); }
        catch (Exception e) { return false; }
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
        return anyDisplayed("table, .plants-list, .data-table, tbody tr, .plant-row, .list-item");
    }

    public void resetToPlantsList() {
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
        if (input == null) return;
        try {
            input.clear();
            typeInto(input, name);
        } catch (Exception ignored) {
        }
    }

    public void setPlantPriceIfPresent(String price) {
        WebElement input = firstDisplayed(
                "input[name*='price'], input#price, input[placeholder*='Price'], input[placeholder*='price']");
        if (input == null) return;
        try {
            input.clear();
            typeInto(input, price);
        } catch (Exception ignored) {
        }
    }

    public void setPlantQuantityIfPresent(String qty) {
        WebElement input = firstDisplayed("input[name*='quantity'], input[name*='stock'], input#quantity, input#stock");
        if (input == null) return;
        try {
            input.clear();
            typeInto(input, qty);
        } catch (Exception ignored) {
        }
    }

    public boolean selectCategoryByName(String name) {
        WebElement selectEl = firstDisplayed(
                "select[name*='sub'], select[name*='category'], select#categoryId, select#category, select.category, select.form-select, .category-select select");
        if (selectEl == null) return false;

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

    public void savePlant() {
        WebElement btn = saveBtn != null ? saveBtn : firstDisplayed("button[type='submit'], .btn-save, .btn-primary");
        if (btn != null) {
            try {
                safeClick(btn);
            } catch (Exception ignored) {
            }
        }
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
                "select[name='categoryId'], select#categoryId, .plants-filters select, select.form-select, select[name*='category']");
        if (selectEl == null) return null;

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
        if (lastSelectedCategoryText != null) return lastSelectedCategoryText;

        WebElement selectEl = firstDisplayed(
                "select[name='categoryId'], select#categoryId, .plants-filters select, select.form-select, select[name*='category']");
        if (selectEl == null) return null;

        try {
            return new org.openqa.selenium.support.ui.Select(selectEl).getFirstSelectedOption().getText().trim();
        } catch (Exception e) {
            return null;
        }
    }

    public void applyFilterIfPresent() {
        WebElement searchBtn = firstDisplayedXpath(
                "//button[normalize-space()='Search' or contains(translate(normalize-space(.),'SEARCH','search'),'search')]");
        if (searchBtn != null) {
            try {
                safeClick(searchBtn);
            } catch (Exception ignored) {
            }
        }

        try {
            waitForPlantsList();
        } catch (Exception ignored) {
        }

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
            if (h == null) continue;
            String norm = h.trim().toLowerCase();
            for (String name : headerNames) {
                String n = name.toLowerCase();
                if (norm.equals(n) || norm.contains(n)) {
                    return i + 1;
                }
            }
        }
        return -1;
    }

    public String getFirstListedPlantCategory() {
        int col = getColumnIndexByHeader("category", "sub category", "subcategory");
        if (col == -1) return null;

        WebElement cell = firstDisplayed("tbody tr:first-child td:nth-child(" + col + ")");
        if (cell == null) return null;

        String text = cell.getText();
        return (text == null || text.trim().isEmpty()) ? null : text.trim();
    }

    public List<String> getVisibleListedPlantCategories() {
        List<String> categories = new java.util.ArrayList<>();

        int col = getColumnIndexByHeader("category", "sub category", "subcategory");
        if (col == -1) return categories;

        List<WebElement> cells = getDriver().findElements(By.cssSelector("tbody tr td:nth-child(" + col + ")"));
        for (WebElement cell : cells) {
            if (cell == null) continue;
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

    public String findFirstPlantNameWithStockAtLeast(int minimumStock) {
        int nameIndex = resolvePlantNameColumnIndex();
        int stockIndex = resolveStockColumnIndex();

        List<WebElement> rows = getVisiblePlantRows();
        for (WebElement row : rows) {
            String plantName = extractPlantNameFromRow(row, nameIndex);
            Integer stock = extractStockFromRow(row, stockIndex);
            if (plantName != null && !plantName.isBlank() && stock != null && stock >= minimumStock) {
                return plantName;
            }
        }
        return null;
    }

    public Integer getStockForPlant(String plantName) {
        if (plantName == null || plantName.isBlank()) return null;

        int nameIndex = resolvePlantNameColumnIndex();
        int stockIndex = resolveStockColumnIndex();

        List<WebElement> rows = getVisiblePlantRows();
        for (WebElement row : rows) {
            String rowPlantName = extractPlantNameFromRow(row, nameIndex);
            if (namesMatch(rowPlantName, plantName)) {
                return extractStockFromRow(row, stockIndex);
            }
        }
        return null;
    }

    private List<WebElement> getVisiblePlantRows() {
        return getDriver().findElements(By.cssSelector("tbody tr, .plant-row, .list-item"));
    }

    private int resolvePlantNameColumnIndex() {
        return findHeaderColumnIndex("name", "plant");
    }

    private int resolveStockColumnIndex() {
        return findHeaderColumnIndex("stock", "quantity", "qty");
    }

    private int findHeaderColumnIndex(String... keywords) {
        List<WebElement> headers = getDriver().findElements(By.cssSelector("table thead th, .table thead th, .plants-list thead th"));
        for (int i = 0; i < headers.size(); i++) {
            String text = safeText(headers.get(i)).toLowerCase(Locale.ENGLISH);
            for (String keyword : keywords) {
                if (text.contains(keyword.toLowerCase(Locale.ENGLISH))) {
                    return i;
                }
            }
        }
        return -1;
    }

    private String extractPlantNameFromRow(WebElement row, int nameIndex) {
        try {
            List<WebElement> cells = row.findElements(By.cssSelector("td"));
            if (nameIndex >= 0 && nameIndex < cells.size()) {
                String fromNamedCell = safeText(cells.get(nameIndex));
                if (!fromNamedCell.isBlank()) return fromNamedCell;
            }

            for (WebElement cell : cells) {
                String cellText = safeText(cell);
                if (cellText.isBlank()) continue;
                if (!cellText.matches("^\\d+$")) {
                    return cellText;
                }
            }

            String rowText = safeText(row);
            return rowText.isBlank() ? null : rowText;
        } catch (Exception e) {
            return null;
        }
    }

    private Integer extractStockFromRow(WebElement row, int stockIndex) {
        try {
            List<WebElement> cells = row.findElements(By.cssSelector("td"));
            if (stockIndex >= 0 && stockIndex < cells.size()) {
                Integer stockFromNamedCell = parseStrictInteger(safeText(cells.get(stockIndex)));
                if (stockFromNamedCell != null) return stockFromNamedCell;
            }

            for (int i = cells.size() - 1; i >= 0; i--) {
                Integer strict = parseStrictInteger(safeText(cells.get(i)));
                if (strict != null) return strict;
            }

            return parseLastInteger(safeText(row));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseStrictInteger(String text) {
        if (text == null) return null;
        String normalized = text.trim().replace(",", "");
        if (!normalized.matches("^\\d+$")) return null;
        try {
            return Integer.parseInt(normalized);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseLastInteger(String text) {
        if (text == null) return null;
        Matcher matcher = Pattern.compile("(\\d+)").matcher(text.replace(",", ""));
        Integer last = null;
        while (matcher.find()) {
            try {
                last = Integer.parseInt(matcher.group(1));
            } catch (Exception ignored) {}
        }
        return last;
    }

    private boolean anyDisplayed(String css) {
        List<WebElement> elements = getDriver().findElements(By.cssSelector(css));
        for (WebElement element : elements) {
            try {
                if (element != null && element.isDisplayed()) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    private String safeText(WebElement element) {
        try {
            String text = element == null ? null : element.getText();
            return text == null ? "" : text.trim();
        } catch (Exception e) {
            return "";
        }
    }

    private boolean namesMatch(String left, String right) {
        if (left == null || right == null) return false;
        String leftNormalized = normalize(left);
        String rightNormalized = normalize(right);
        if (leftNormalized.isBlank() || rightNormalized.isBlank()) return false;
        return leftNormalized.equals(rightNormalized)
                || leftNormalized.contains(rightNormalized)
                || rightNormalized.contains(leftNormalized);
    }

    private String normalize(String text) {
        return text.toLowerCase(Locale.ENGLISH).replaceAll("\\s+", " ").trim();
    }

    private void waitForPlantsList() {
        withTimeoutOf(Duration.ofSeconds(10)).waitForCondition()
                .until(driver -> driver.getCurrentUrl().contains("/ui/plants"));

        withTimeoutOf(Duration.ofSeconds(10)).waitForCondition()
                .until(driver -> isListVisible());
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
        if (el == null) return;
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
