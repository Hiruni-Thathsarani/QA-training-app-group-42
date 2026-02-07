package pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class SalesPage extends PageObject {

    @FindBy(css = "h1, h2, .page-title")
    WebElement title;

    public boolean isAt() {
        try { return title != null && title.isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public boolean isListVisible() {
        return anyDisplayed("table, .sales-list, .data-table, tbody tr, .sale-row, .list-item");
    }

    public boolean sellActionVisible() {
        return anyDisplayed("a[href*='sales/new'], a[href*='sell'], button.sell, .btn-sell, .sell-action");
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
}
