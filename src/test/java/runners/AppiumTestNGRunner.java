package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import utils.EmulatorManager;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = "src/test/resources/features", // Path to your feature files
    glue = "appium",           // Package containing step definitions
    plugin = {"pretty", "html:target/cucumber-reports.html", "json:target/cucumber.json"},
    monochrome = true
)
public class AppiumTestNGRunner extends AbstractTestNGCucumberTests {

    @BeforeSuite(alwaysRun = true)
    public void setupEmulator() throws Exception {
        System.out.println("Starting emulator setup process...");

        try {
            // Verify tools availability
            if (!EmulatorManager.isCommandAvailable("sdkmanager") 
                || !EmulatorManager.isCommandAvailable("avdmanager") 
                || !EmulatorManager.isCommandAvailable("emulator")) {
                throw new RuntimeException("Android SDK tools are missing. Ensure they are installed and available in PATH.");
            }

            // Run emulator and install APK
            EmulatorManager.setupEmulatorAndInstallApp();
            System.out.println("Emulator setup and APK installation completed successfully.");
        } catch (Exception e) {
            System.err.println("Error during emulator setup: " + e.getMessage());
            throw e;
        }
    }

    @Override
    @DataProvider(parallel = true) // Enable parallel execution if needed
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
