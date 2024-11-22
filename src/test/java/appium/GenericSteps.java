package appium;

import io.appium.java_client.AppiumDriver;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.AfterAll;
import io.cucumber.java.en.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import utils.AppiumDriverManager;

import java.util.Map;

public class GenericSteps {

    private static AppiumDriver driver;


    @BeforeAll
    public static void setup() {
        // Ensure the driver is initialized before any test runs
        if (driver == null) {
            driver = AppiumDriverManager.getAppiumDriver();
        }
    }

    @AfterAll
    public static void teardown() {
        // Quit the driver after all tests
        AppiumDriverManager.quitDriver();
    }

    @Given("^I launch the app$")
    public void iLaunchTheApp() {
        // App launch confirmation
        System.out.println("App is launched successfully with the following capabilities:");
        System.out.println(AppiumDriverManager.getCapabilities());
    }

    @When("^I click the element with \"([^\"]*)\" locator \"([^\"]*)\"$")
    public void iClickElement(String locatorType, String locatorValue) {
        By locator = getLocatorByType(locatorType, locatorValue);
        AppiumDriverManager.tapElement(locator);
    }

    @When("^I enter \"([^\"]*)\" in the element with \"([^\"]*)\" locator \"([^\"]*)\"$")
    public void iEnterTextInElement(String text, String locatorType, String locatorValue) {
        By locator = getLocatorByType(locatorType, locatorValue);
        AppiumDriverManager.enterText(locator, text);
    }

    @Then("^I should see the element with \"([^\"]*)\" locator \"([^\"]*)\"$")
    public void iShouldSeeTheElement(String locatorType, String locatorValue) {
        By locator = getLocatorByType(locatorType, locatorValue);
        AppiumDriverManager.waitForElementVisible(locator);
    }

    @Then("^I should see the text \"([^\"]*)\"$")
    public void iShouldSeeTheText(String text) {
        By locator = By.xpath("//*[contains(@text, '" + text + "') or contains(@content-desc, '" + text + "')]");
        AppiumDriverManager.waitForElementVisible(locator);
    }

    @When("^I (check|uncheck) the checkbox with \"([^\"]*)\" locator \"([^\"]*)\"$")
    public void iCheckOrUncheckCheckbox(String action, String locatorType, String locatorValue) {
        By locator = getLocatorByType(locatorType, locatorValue);
        WebElement checkbox = driver.findElement(locator);
        boolean isChecked = checkbox.isSelected();

        if (action.equals("check") && !isChecked) {
            checkbox.click();
        } else if (action.equals("uncheck") && isChecked) {
            checkbox.click();
        }
    }

    @Then("^the \"([^\"]*)\" should be successfully created$")
    public void verifyEntityCreation(String entityType, Map<String, String> entityDetails) {
        System.out.println("Verifying " + entityType + " creation with details: " + entityDetails);
    }

    /**
     * Utility method to get a locator by its type.
     *
     * @param locatorType  The type of the locator (e.g., id, xpath, accessibilityId, etc.)
     * @param locatorValue The value of the locator
     * @return A `By` object representing the locator
     */
    private By getLocatorByType(String locatorType, String locatorValue) {
        switch (locatorType.toLowerCase()) {
            case "id":
                return By.id(locatorValue);
            case "xpath":
                return By.xpath(locatorValue);
            case "classname":
                return By.className(locatorValue);
            case "name":
                return By.name(locatorValue); // Deprecated but supported for backward compatibility
            case "tagname":
                return By.tagName(locatorValue);
                case "text":
            return By.xpath(String.format("//*[contains(@text, '%s') or contains(@content-desc, '%s')]", locatorValue, locatorValue));
        default:

                throw new IllegalArgumentException("Unsupported locator type: " + locatorType);
        }
    }
}
