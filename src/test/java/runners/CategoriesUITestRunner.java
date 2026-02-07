package runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features/ui/categories_ui.feature",
        glue = {"stepdefinitions.ui"},
        plugin = {"pretty"}
)
public class CategoriesUITestRunner {}
