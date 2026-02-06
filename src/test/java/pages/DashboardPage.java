package pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.annotations.findby.FindBy;
import org.openqa.selenium.WebElement;

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
}
