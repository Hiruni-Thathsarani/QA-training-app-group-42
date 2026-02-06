package pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.annotations.findby.FindBy;
import org.openqa.selenium.WebElement;
import utils.Urls;

public class LoginPage extends PageObject {

    @FindBy(name = "username")
    WebElement username;

    @FindBy(name = "password")
    WebElement password;

    @FindBy(css = "button[type='submit']")
    WebElement loginBtn;

    @FindBy(css = ".alert-danger, .error, .invalid-feedback, .toast, .snackbar")
    WebElement errorMsg;

    public void openLoginPage() {
        openUrl(Urls.UI_LOGIN);
    }

    public void login(String user, String pass) {
        typeInto(username, user);
        typeInto(password, pass);
        loginBtn.click();
    }

    public boolean isErrorVisible() {
        try {
            return errorMsg != null && errorMsg.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
