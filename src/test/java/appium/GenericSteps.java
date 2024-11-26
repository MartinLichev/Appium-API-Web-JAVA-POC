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
    public static void setupDriver() {
        // Ensure the driver is initialized before any test runs
        if (driver == null) {
            driver = AppiumDriverManager.getAppiumDriver();
        }
    }

    @AfterAll
    public static void quitDriver() {
        // Quit the driver after all tests
        AppiumDriverManager.quitDriver();
    }

    @Given("the application is launched")
    public void theApplicationIsLaunched() {
        // App launch confirmation
        System.out.println("App is launched successfully with the following capabilities:");
        System.out.println(AppiumDriverManager.getCapabilities());
    }

    @When("I tap the element identified by {string} with value {string}")
    public void iTapElement(String locatorType, String locatorValue) {
        By locator = getLocatorByType(locatorType, locatorValue);
        AppiumDriverManager.tapElement(locator);
    }

    @When("I input {string} into the field identified by {string} with value {string}")
    public void iInputTextIntoField(String text, String locatorType, String locatorValue) {
        By locator = getLocatorByType(locatorType, locatorValue);
        AppiumDriverManager.enterText(locator, text);
    }

    @Then("the element identified by {string} with value {string} should be visible")
    public void theElementShouldBeVisible(String locatorType, String locatorValue) {
        By locator = getLocatorByType(locatorType, locatorValue);
        AppiumDriverManager.waitForElementVisible(locator);
    }

    @Then("the text {string} should be visible on the screen")
    public void theTextShouldBeVisible(String text) {
        By locator = By.xpath("//*[contains(@text, '" + text + "') or contains(@content-desc, '" + text + "')]");
        AppiumDriverManager.waitForElementVisible(locator);
    }

    @When("I {string} the checkbox identified by {string} with value {string}")
    public void iToggleCheckbox(String action, String locatorType, String locatorValue) {
        By locator = getLocatorByType(locatorType, locatorValue);
        WebElement checkbox = driver.findElement(locator);
        boolean isChecked = checkbox.isSelected();

        if (action.equals("check") && !isChecked) {
            checkbox.click();
        } else if (action.equals("uncheck") && isChecked) {
            checkbox.click();
        }
    }

    @Then("a new {string} entity should be successfully created")
    public void verifyEntityCreation(String entityType, Map<String, String> entityDetails) {
        System.out.println("Verifying " + entityType + " creation with details: " + entityDetails);
    }

    /**
     * Utility method to get a locator by its type.
     *
     * @param locatorType  The type of the locator (e.g., id, xpath,
     *                     accessibilityId, etc.)
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
                return By.name(locatorValue);
            case "tagname":
                return By.tagName(locatorValue);
            case "text":
                return By.xpath(String.format("//*[contains(@text, '%s') or contains(@content-desc, '%s')]",
                        locatorValue, locatorValue));
            case "accessibilityid":
                return By.xpath(String.format("//*[@content-desc='%s']", locatorValue));
            case "attribute":
                String[] parts = locatorValue.split("=", 2);
                if (parts.length == 2) {
                    String attributeName = parts[0];
                    String attributeValue = parts[1];
                    return By.xpath(String.format("//*[@%s='%s']", attributeName, attributeValue));
                } else {
                    throw new IllegalArgumentException(
                            "Invalid attribute locator value. Expected format: 'attribute=value'");
                }
            default:
                throw new IllegalArgumentException("Unsupported locator type: " + locatorType);
        }
    }
}
