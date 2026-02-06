package pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.annotations.findby.FindBy;
import org.openqa.selenium.WebElement;

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
}
