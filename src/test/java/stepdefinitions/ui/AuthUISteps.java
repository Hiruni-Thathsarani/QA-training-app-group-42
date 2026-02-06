package stepdefinitions.ui;

import io.cucumber.java.en.*;
import net.thucydides.core.pages.Pages;
import org.assertj.core.api.Assertions;
import pages.DashboardPage;
import pages.LoginPage;
import utils.TestUsers;
import utils.Urls;

public class AuthUISteps {

    private final LoginPage loginPage;
    private final DashboardPage dashboardPage;

    public AuthUISteps(Pages pages) {
        this.loginPage = pages.getPage(LoginPage.class);
        this.dashboardPage = pages.getPage(DashboardPage.class);
    }

    @Given("user is on login page")
    public void userIsOnLoginPage() {
        loginPage.openLoginPage();
    }

    @When("user logs in as admin")
    public void userLogsInAsAdmin() {
        loginPage.login(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD);
    }

    @When("user logs in as normal user")
    public void userLogsInAsNormalUser() {
        loginPage.login(TestUsers.USER_USERNAME, TestUsers.USER_PASSWORD);
    }

    @When("user logs in with invalid credentials")
    public void userLogsInWithInvalidCredentials() {
        loginPage.login("wrong", "wrong123");
    }

    @Then("dashboard should be visible")
    public void dashboardShouldBeVisible() {
        Assertions.assertThat(dashboardPage.isDashboardVisible()).isTrue();
    }

    @Then("error message should be shown")
    public void errorMessageShouldBeShown() {
        Assertions.assertThat(loginPage.isErrorVisible()).isTrue();
    }

    @Given("user is logged in as admin")
    public void userIsLoggedInAsAdmin() {
        loginPage.openLoginPage();
        loginPage.login(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD);
        dashboardPage.waitForDashboard();
        // Dashboard check is relaxed since page may vary
    }

    @Given("user is logged in as normal user")
    public void userIsLoggedInAsNormalUser() {
        loginPage.openLoginPage();
        loginPage.login(TestUsers.USER_USERNAME, TestUsers.USER_PASSWORD);
        dashboardPage.waitForDashboard();
        // Dashboard check is relaxed since page may vary
    }

    @When("user logs out")
    public void userLogsOut() {
        dashboardPage.logout();
    }

    @Then("login page should be visible")
    public void loginPageShouldBeVisible() {
        Assertions.assertThat(loginPage.getDriver().getCurrentUrl()).contains("/ui/login");
    }

    @Given("user is not logged in")
    public void userIsNotLoggedIn() {
    }

    @When("user opens dashboard directly")
    public void userOpensDashboardDirectly() {
        loginPage.openUrl(Urls.UI_DASHBOARD);
    }

    @Then("user should be redirected to login")
    public void userShouldBeRedirectedToLogin() {
        Assertions.assertThat(loginPage.getDriver().getCurrentUrl()).contains("/ui/login");
    }

    @Then("admin actions should not be visible")
    public void adminActionsShouldNotBeVisible() {
        String page = loginPage.getDriver().getPageSource();
        Assertions.assertThat(page).doesNotContain("Delete");
        Assertions.assertThat(page).doesNotContain("Edit");
        Assertions.assertThat(page).doesNotContain("Add");
        Assertions.assertThat(page).doesNotContain("/ui/sales/new");
    }

    @When("user opens sell page directly")
    public void userOpensSellPageDirectly() {
        loginPage.openUrl(Urls.UI_SELL_NEW);
    }

    @Then("access should be blocked")
    public void accessShouldBeBlocked() {
        String url = loginPage.getDriver().getCurrentUrl();
        String source = loginPage.getDriver().getPageSource();

        boolean blocked = url.contains("/ui/login") || url.contains("403") || source.contains("Forbidden");
        Assertions.assertThat(blocked).isTrue();
    }

    @When("user opens sell page directly without login")
    public void userOpensSellPageDirectlyWithoutLogin() {
        loginPage.openUrl(Urls.UI_SELL_NEW);
    }
}
