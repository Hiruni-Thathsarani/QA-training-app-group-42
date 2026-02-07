package pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SalesPage extends PageObject {

    @FindBy(css = "h1, h2, .page-title")
    WebElement title;

    public boolean isAt() {
        try {
            String url = getDriver().getCurrentUrl();
            boolean urlMatches = url != null && url.contains("/ui/sales");
            boolean titleVisible = title != null && title.isDisplayed();
            return urlMatches || titleVisible;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isListVisible() {
        return anyDisplayedCss("table, .sales-list, .data-table, tbody tr, .sale-row, .list-item");
    }

    public boolean hasUiErrorBanner() {
        return anyDisplayedCss(".alert-danger, .error, .invalid-feedback, .toast.error, .snackbar.error");
    }

    public boolean sellPlantVisible() {
        if (anyDisplayedCss("a[href*='sales/new'], button.add-sale, .btn-add, a[href*='sell']")) {
            return true;
        }
        return anyDisplayedXpath("//a[contains(.,'Sell Plant')] | //button[contains(.,'Sell Plant')] | //a[contains(.,'Sell')]");
    }

    public boolean deleteActionVisible() {
        if (anyDisplayedCss("button.delete, .btn-delete, a[href*='delete'], [data-testid*='delete']")) {
            return true;
        }
        return anyDisplayedXpath("//button[contains(.,'Delete')] | //a[contains(.,'Delete')]");
    }

    public boolean editableFieldsVisible() {
        return anyDisplayedCss(
                "tbody input:not([readonly]):not([disabled]), tbody select:not([disabled]), tbody textarea:not([readonly]):not([disabled]), tbody [contenteditable='true'], .sales-list input:not([readonly]):not([disabled]), .sales-list select:not([disabled]), .sales-list textarea:not([readonly]):not([disabled])"
        );
    }

    public boolean readOnlyForUser() {
        return !sellPlantVisible() && !deleteActionVisible() && !editableFieldsVisible();
    }

    public void goToNextPage() {
        WebElement next = findNextPageElement();
        if (next == null) return;
        try {
            next.click();
        } catch (Exception ignored) {}
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
        WebElement active = firstDisplayedCss(".pagination .active, .page-item.active, li.active");
        if (active == null) return null;
        try {
            String text = active.getText();
            if (text == null) return null;
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return null;
        }
    }

    public String getFirstRowSnapshot() {
        WebElement row = firstDisplayedCss("tbody tr, .sale-row, .list-item");
        if (row == null) return null;
        try {
            String text = row.getText();
            if (text == null) return null;
            String normalized = text.replaceAll("\\s+", " ").trim();
            return normalized.isEmpty() ? null : normalized;
        } catch (Exception e) {
            return null;
        }
    }

    public List<LocalDateTime> getVisibleSoldDates() {
        List<LocalDateTime> soldDates = new ArrayList<>();
        List<WebElement> rows = getDriver().findElements(By.cssSelector("tbody tr, .sale-row, .list-item"));
        for (WebElement row : rows) {
            try {
                if (row == null || !row.isDisplayed()) continue;
                String rowText = row.getText();
                LocalDateTime parsed = parseDateTimeFromText(rowText);
                if (parsed != null) {
                    soldDates.add(parsed);
                }
            } catch (Exception ignored) {}
        }
        return soldDates;
    }

    public boolean soldDatesSortedDesc() {
        List<LocalDateTime> values = getVisibleSoldDates();
        if (values.size() < 2) return false;

        for (int i = 0; i < values.size() - 1; i++) {
            LocalDateTime current = values.get(i);
            LocalDateTime next = values.get(i + 1);
            if (current.isBefore(next)) {
                return false;
            }
        }
        return true;
    }

    private WebElement findNextPageElement() {
        WebElement next = firstDisplayedCss("a[rel='next'], .pagination .next a, .pagination .page-item.next a, .pagination-next, a[aria-label*='Next'], button[aria-label*='Next']");
        if (next != null) return next;

        List<WebElement> candidates = getDriver().findElements(By.xpath("//a[contains(.,'Next')] | //button[contains(.,'Next')] | //a[contains(.,'›')] | //button[contains(.,'›')] | //a[contains(.,'>')] | //button[contains(.,'>')]"));
        for (WebElement candidate : candidates) {
            try {
                if (candidate != null && candidate.isDisplayed()) return candidate;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private boolean anyDisplayedCss(String css) {
        List<WebElement> elements = getDriver().findElements(By.cssSelector(css));
        for (WebElement element : elements) {
            try {
                if (element != null && element.isDisplayed()) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    private WebElement firstDisplayedCss(String css) {
        List<WebElement> elements = getDriver().findElements(By.cssSelector(css));
        for (WebElement element : elements) {
            try {
                if (element != null && element.isDisplayed()) return element;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private boolean anyDisplayedXpath(String xpath) {
        List<WebElement> elements = getDriver().findElements(By.xpath(xpath));
        for (WebElement element : elements) {
            try {
                if (element != null && element.isDisplayed()) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    private LocalDateTime parseDateTimeFromText(String text) {
        if (text == null) return null;
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) return null;

        List<DateTimeFormatter> dateTimeFormats = Arrays.asList(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
                DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("MMMM d, yyyy HH:mm", Locale.ENGLISH)
        );

        List<DateTimeFormatter> dateFormats = Arrays.asList(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)
        );

        String[] tokens = normalized.split(" ");
        for (int start = 0; start < tokens.length; start++) {
            for (int end = Math.min(tokens.length, start + 4); end > start; end--) {
                String candidate = String.join(" ", Arrays.copyOfRange(tokens, start, end)).trim();
                if (candidate.isEmpty()) continue;

                for (DateTimeFormatter formatter : dateTimeFormats) {
                    try {
                        return LocalDateTime.parse(candidate, formatter);
                    } catch (DateTimeParseException ignored) {}
                }

                for (DateTimeFormatter formatter : dateFormats) {
                    try {
                        LocalDate date = LocalDate.parse(candidate, formatter);
                        return LocalDateTime.of(date, LocalTime.MIDNIGHT);
                    } catch (DateTimeParseException ignored) {}
                }
            }
        }

        return null;
    }
}
