// DashboardPage.java
package pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.annotations.findby.FindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.Urls;

import java.time.Duration;

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

    public void waitForDashboard() {
        try {
            WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(10));
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/ui/dashboard"),
                    ExpectedConditions.urlContains("/ui/categories"),
                    ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("h1, h2, .dashboard-title, .sidebar, nav"))));
        } catch (Exception ignored) {
        }
    }

    public void logout() {
        try {
            if (logoutBtn != null) logoutBtn.click();
        } catch (Exception ignored) {}
        try {
            if (!getDriver().getCurrentUrl().contains("/ui/login")) {
                openUrl(Urls.UI_LOGOUT);
            }
        } catch (Exception ignored) {}
    }
}
