package pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.annotations.findby.FindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

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

    public boolean isAt() {
        try { return title != null && title.isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public boolean adminActionsVisible() {
        // if any admin action is visible, return true
        try {
            return (addBtn != null && addBtn.isDisplayed())
                    || (editBtn != null && editBtn.isDisplayed())
                    || (deleteBtn != null && deleteBtn.isDisplayed());
        } catch (Exception e) {
            return false;
        }
    }

    // ===== NEW CODE - ADMIN SALES UI TESTS START =====
    public boolean isListVisible() {
        return anyDisplayed("table, .plants-list, .data-table, tbody tr, .plant-row, .list-item");
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
    // ===== NEW CODE - ADMIN SALES UI TESTS END =====
}
