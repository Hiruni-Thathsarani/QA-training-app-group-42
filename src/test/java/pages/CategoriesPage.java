package pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.annotations.findby.FindBy;
import org.openqa.selenium.WebElement;

public class CategoriesPage extends PageObject {

    @FindBy(css = "h1, h2, .page-title")
    WebElement title;

    @FindBy(css = "a[href*='categories/new'], button.add-category, .btn-add")
    WebElement addBtn;

    @FindBy(css = "button.edit, a[href*='edit'], .btn-edit")
    WebElement editBtn;

    @FindBy(css = "button.delete, .btn-delete")
    WebElement deleteBtn;

    public boolean isAt() {
        try { return title != null && title.isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public boolean addVisible() {
        try { return addBtn != null && addBtn.isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public boolean editVisible() {
        try { return editBtn != null && editBtn.isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public boolean deleteVisible() {
        try { return deleteBtn != null && deleteBtn.isDisplayed(); }
        catch (Exception e) { return false; }
    }
}
