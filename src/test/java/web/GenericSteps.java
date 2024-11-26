package web;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.AfterAll;
import io.cucumber.java.en.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.WebDriverManager;

import java.util.concurrent.TimeUnit;

public class GenericSteps {
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        driver = WebDriverManager.getWebDriver();
        wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10)); // Explicit wait with a timeout of 10
                                                                            // seconds
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    @AfterAll
    public static void teardown() {
        WebDriverManager.quitDriver();
    }

    @Given("^I navigate to the URL \"([^\"]*)\"$")
    public void navigateToUrl(String url) {
        driver.get(url);
    }

    @When("^I click the \"([^\"]*)\" (button|link)$")
    public void clickElement(String elementText, String elementType) {
        By locator = getLocatorForText(elementText, elementType);
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    @When("^I enter \"([^\"]*)\" in the \"([^\"]*)\" (field|input|textarea)$")
    public void enterText(String text, String label, String elementType) {
        By locator = getLocatorForLabel(label, elementType);
        WebElement element = driver.findElement(locator);
        element.clear();
        element.sendKeys(text);
    }

    @Then("^I should see the \"([^\"]*)\" (field|button|checkbox|link|textarea)$")
    public void verifyElementVisible(String label, String elementType) {
        By locator = getLocatorForLabel(label, elementType);
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    @Then("^I should see the \"([^\"]*)\" text$")
    public void verifyTextVisible(String text) {
        By locator = By.xpath("//*[contains(text(), '" + text + "')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    @Then("^the page title should be \"([^\"]*)\"$")
    public void verifyPageTitle(String expectedTitle) {
        wait.until(ExpectedConditions.titleIs(expectedTitle));
    }

    @When("^I press the \"([^\"]*)\" key$")
    public void pressKey(String key) {
        Actions actions = new Actions(driver);
        actions.sendKeys(key).perform();
    }

    // Utility method for getting locator by text and type
    private By getLocatorForText(String text, String elementType) {
        switch (elementType.toLowerCase()) {
            case "button":
                return By.xpath("//button[contains(text(), '" + text + "')]");
            case "link":
                return By.xpath("//a[contains(text(), '" + text + "')]");
            default:
                throw new IllegalArgumentException("Unknown element type: " + elementType);
        }
    }

    // Utility method for getting locator by label and type
    private By getLocatorForLabel(String label, String elementType) {
        switch (elementType.toLowerCase()) {
            case "field":
            case "input":
                return By.xpath("//input[@placeholder='" + label + "'] | //label[contains(text(), '" + label
                        + "')]/following-sibling::input");
            case "textarea":
                return By.xpath("//textarea[@placeholder='" + label + "']");
            case "button":
                return By.xpath("//button[contains(text(), '" + label + "')]");
            case "checkbox":
                return By
                        .xpath("//label[contains(text(), '" + label + "')]/preceding-sibling::input[@type='checkbox']");
            case "link":
                return By.xpath("//a[contains(text(), '" + label + "')]");
            default:
                throw new IllegalArgumentException("Unknown element type: " + elementType);
        }
    }
}
