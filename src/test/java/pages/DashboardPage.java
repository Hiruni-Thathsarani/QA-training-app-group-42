package pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.annotations.findby.FindBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import utils.Urls;

import java.util.List;

public class DashboardPage extends PageObject {

    @FindBy(css = "h1, h2, .dashboard-title")
    WebElement dashboardTitle;

    @FindBy(css = "a[href*='logout'], button.logout")
    WebElement logoutBtn;

    public boolean isDashboardVisible() {
        try {
            return dashboardTitle != null && dashboardTitle.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void logout() {
        try {
            if (logoutBtn != null) logoutBtn.click();
        } catch (Exception ignored) {}
    }

    public void openInventory() {
        WebElement link = firstDisplayed("a[href*='plants'], a[href*='inventory'], a[href*='ui/plants'], nav a");
        if (link != null) {
            try { link.click(); return; } catch (Exception ignored) {}
        }
        openUrl(Urls.UI_PLANTS);
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
}
