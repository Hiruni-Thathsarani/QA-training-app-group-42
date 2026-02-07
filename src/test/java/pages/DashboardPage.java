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
        try {
            if (!getDriver().getCurrentUrl().contains("/ui/login")) {
                openUrl(utils.Urls.UI_LOGOUT);
            }
        } catch (Exception ignored) {}
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
