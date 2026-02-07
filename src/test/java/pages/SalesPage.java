package pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // ===== NEW CODE - USER SALES LIST TESTS START =====
    public boolean createSaleActionVisible() {
        if (anyDisplayedCss("a[href*='sales/new'], button.add-sale, .btn-add, a[href*='sell'], [data-testid*='create-sale'], [data-testid*='sell']")) {
            return true;
        }
        return anyDisplayedXpath(
                "//a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sell plant')]"
                        + " | //button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sell plant')]"
                        + " | //a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'create sale')]"
                        + " | //button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'create sale')]"
                        + " | //a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add sale')]"
                        + " | //button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add sale')]"
        );
    }

    public boolean hasActionColumn() {
        if (anyDisplayedCss("th.actions, td.actions, [data-testid*='action'], [data-testid*='actions'], .col-actions")) {
            return true;
        }
        return anyDisplayedXpath(
                "//th[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'action')]"
                        + " | //th[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'delete')]"
                        + " | //th[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'edit')]"
                        + " | //th[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'manage')]"
        );
    }

    public boolean hasExpectedReadOnlyColumns() {
        return hasColumnHeader("Plant")
                && hasColumnHeader("Quantity")
                && hasColumnHeader("Total Price")
                && hasColumnHeader("Sold At")
                && !hasActionColumn();
    }

    private boolean hasColumnHeader(String headerText) {
        String lower = headerText.toLowerCase(Locale.ENGLISH);
        List<WebElement> headers = getDriver().findElements(By.cssSelector("table thead th, .table thead th, .sales-list thead th"));
        for (WebElement header : headers) {
            try {
                if (header == null || !header.isDisplayed()) continue;
                String text = header.getText();
                if (text != null && text.trim().toLowerCase(Locale.ENGLISH).contains(lower)) return true;
            } catch (Exception ignored) {}
        }
        List<WebElement> anyHeaders = getDriver().findElements(By.xpath("//th"));
        for (WebElement header : anyHeaders) {
            try {
                if (header == null || !header.isDisplayed()) continue;
                String text = header.getText();
                if (text != null && text.trim().toLowerCase(Locale.ENGLISH).contains(lower)) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }
    // ===== NEW CODE - USER SALES LIST TESTS END =====

    // ===== NEW CODE - USER SALES LIST TESTS START =====
    public boolean isSellPlantFormVisible() {
        return isAtSellPlantPage() && anyDisplayedCss("form, .sell-form, .sale-form, .card form");
    }

    public boolean isAtSellPlantPage() {
        try {
            String url = getDriver().getCurrentUrl();
            boolean urlMatches = url != null && (url.contains("/ui/sales/new") || url.contains("/sales/new"));
            if (urlMatches) {
                return true;
            }

            boolean sellPlantHeadingVisible = anyDisplayedXpath(
                    "//*[self::h1 or self::h2 or self::h3]"
                            + "[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sell plant')]"
            );
            boolean sellFormVisible = anyDisplayedCss("form, .sell-form, .sale-form, .card form");
            return sellPlantHeadingVisible && sellFormVisible;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean plantSelectionVisible() {
        return firstDisplayedCss("select[name*='plant'], select#plant, [data-testid*='plant'] select") != null;
    }

    public boolean quantityFieldVisible() {
        return firstDisplayedCss("input[name*='quantity'], input#quantity, input[type='number']") != null;
    }

    public boolean submitSellVisible() {
        if (firstDisplayedCss("button[type='submit'], .btn-primary, .btn-submit") != null) {
            return true;
        }
        return anyDisplayedXpath("//button[contains(.,'Submit')] | //button[contains(.,'Sell')] | //button[contains(.,'Save')]");
    }

    public boolean hasAccessOrAuthorizationError() {
        String page = "";
        try { page = getDriver().getPageSource(); } catch (Exception ignored) {}
        String lower = page == null ? "" : page.toLowerCase(Locale.ENGLISH);
        boolean authText = lower.contains("forbidden")
                || lower.contains("unauthorized")
                || lower.contains("access denied")
                || lower.contains("403");
        return authText || hasUiErrorBanner();
    }

    public boolean selectFirstPlantForSale() {
        WebElement plantSelect = firstDisplayedCss("select[name*='plant'], select#plant, [data-testid*='plant'] select");
        if (plantSelect == null) return false;
        try {
            Select select = new Select(plantSelect);
            for (WebElement option : select.getOptions()) {
                String value = option.getAttribute("value");
                String text = option.getText();
                if (value != null && !value.trim().isEmpty() && text != null && !text.trim().isEmpty()) {
                    select.selectByValue(value);
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    public void clearQuantityForSale() {
        WebElement quantity = firstDisplayedCss("input[name*='quantity'], input#quantity, input[type='number']");
        if (quantity == null) return;
        try {
            quantity.clear();
            quantity.sendKeys("");
        } catch (Exception ignored) {}
    }

    public void enterOversellQuantity() {
        WebElement quantity = firstDisplayedCss("input[name*='quantity'], input#quantity, input[type='number']");
        if (quantity == null) return;
        int oversell = resolveOversellQuantity();
        try {
            quantity.clear();
            quantity.sendKeys(String.valueOf(oversell));
        } catch (Exception ignored) {}
    }

    public void submitSellForm() {
        WebElement submit = firstDisplayedCss("button[type='submit'], .btn-primary, .btn-submit");
        if (submit != null) {
            try {
                submit.click();
                return;
            } catch (Exception ignored) {}
        }
        WebElement fallback = firstDisplayedXpath("//button[contains(.,'Submit')] | //button[contains(.,'Sell')] | //button[contains(.,'Save')]");
        if (fallback != null) {
            try { fallback.click(); } catch (Exception ignored) {}
        }
    }

    public boolean quantityValidationErrorVisible() {
        if (anyDisplayedCss("input[name*='quantity']:invalid, input#quantity:invalid")) {
            return true;
        }
        return anyDisplayedXpath(
                "//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'quantity is required')]"
                        + " | //*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'quantity required')]"
                        + " | //*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'must not be blank')]"
                        + " | //*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'required')]"
        );
    }

    public boolean insufficientStockErrorVisible() {
        return anyDisplayedXpath(
                "//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'insufficient stock')]"
                        + " | //*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'not enough stock')]"
                        + " | //*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'exceeds available stock')]"
                        + " | //*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'available stock')]"
        ) || hasUiErrorBanner();
    }

    public boolean saleCreationSuccessVisible() {
        if (anyDisplayedCss(".alert-success, .toast-success, .snackbar.success")) {
            return true;
        }
        return anyDisplayedXpath(
                "//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sale created')]"
                        + " | //*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'successfully sold')]"
                        + " | //*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'created successfully')]"
        );
    }

    private int resolveOversellQuantity() {
        int detected = detectAvailableStockFromSelectedPlant();
        if (detected > 0) return detected + 1;
        return 999999;
    }

    private int detectAvailableStockFromSelectedPlant() {
        WebElement plantSelect = firstDisplayedCss("select[name*='plant'], select#plant, [data-testid*='plant'] select");
        if (plantSelect == null) return -1;
        try {
            Select select = new Select(plantSelect);
            WebElement selected = select.getFirstSelectedOption();
            if (selected == null) return -1;
            String text = selected.getText();
            if (text == null) return -1;
            Matcher matcher = Pattern.compile("(\\d+)").matcher(text);
            int last = -1;
            while (matcher.find()) {
                last = Integer.parseInt(matcher.group(1));
            }
            return last;
        } catch (Exception ignored) {
            return -1;
        }
    }

    private WebElement firstDisplayedXpath(String xpath) {
        List<WebElement> elements = getDriver().findElements(By.xpath(xpath));
        for (WebElement element : elements) {
            try {
                if (element != null && element.isDisplayed()) return element;
            } catch (Exception ignored) {}
        }
        return null;
    }
    // ===== NEW CODE - USER SALES LIST TESTS END =====
}
