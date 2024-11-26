package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

@CucumberOptions(features = "src/test/resources/features/API", // Path to your API feature files
        glue = "api", // Package containing step definitions
        plugin = { "pretty", "html:target/api-cucumber-reports.html",
                "json:target/api-cucumber.json" }, monochrome = true)
public class APITestNGRunner extends AbstractTestNGCucumberTests {

    @BeforeSuite(alwaysRun = true)
    public void setupAPITests() {
        System.out.println("Setting up API tests...");
    }

    @AfterSuite(alwaysRun = true)
    public void teardownAPITests() {
        System.out.println("Tearing down API tests...");
    }

    @Override
    @DataProvider(parallel = true) // Enable parallel execution if needed
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
