package runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(features = "src/test/resources/features/api", glue = { "stepdefinitions.api" }, plugin = {
                "pretty" }, tags = "")
public class APITestRunner {
}
