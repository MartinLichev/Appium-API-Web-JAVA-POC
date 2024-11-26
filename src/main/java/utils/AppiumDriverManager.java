package utils;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import io.github.cdimascio.dotenv.Dotenv;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.util.Collections;
import java.util.Map;

public class AppiumDriverManager {
    private static AppiumDriver driver;
    private static final int DEFAULT_WAIT_TIME = 10; // Default wait time in seconds
    private static Dotenv dotenv;
    private static final String APK_URL_STRING = "/src/test/resources/binaries/android/com.kpmoney.android_3a.13.19-362_minAPI26(arm64-v8a,armeabi,armeabi-v7a,mips,mips64,x86,x86_64)(nodpi)_apkmirror.com.apk";

    // Load .env file
    static {
        dotenv = Dotenv.configure().load();
    }

    /**
     * Initialize and return the AppiumDriver
     */
    public static AppiumDriver getAppiumDriver() {
        if (driver == null) {
            try {
                // Use UiAutomator2Options for Android
                UiAutomator2Options options = new UiAutomator2Options()
                        .setPlatformName(dotenv.get("PLATFORM_NAME"))
                        .setDeviceName(dotenv.get("DEVICE_NAME"))
                        .setAutomationName(dotenv.get("AUTOMATION_NAME"))
                        .setPlatformVersion(dotenv.get("PLATFORM_VERSION"))
                        .setAppPackage(dotenv.get("APP_PACKAGE"))
                        .setAppActivity(dotenv.get("APP_ACTIVITY"))
                        .setNoReset(Boolean.parseBoolean(dotenv.get("APPIUM_NORESET")))
                        .setApp(System.getProperty("user.dir") + APK_URL_STRING);

                // Initialize the Appium driver
                driver = new AppiumDriver(new URL(dotenv.get("APPIUM_SERVER")), options);

            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize AppiumDriver", e);
            }
        }
        return driver;
    }

    public static Map<String, Object> getCapabilities() {
        if (driver == null) {
            throw new IllegalStateException("Driver not initialized. Call getAppiumDriver() first.");
        }
        // Return the capabilities as a map
        return driver.getCapabilities().asMap();
    }

    /**
     * Quit the AppiumDriver
     */
    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    /**
     * Wait for an element to be visible
     *
     * @param locator Locator for the element
     * @return MobileElement once it becomes visible
     */
    public static WebElement waitForElementVisible(By locator) {
        WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(DEFAULT_WAIT_TIME));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Scroll down the screen
     */
    public static void scrollDown() {
        Dimension dimension = driver.manage().window().getSize();
        int startX = dimension.width / 2;
        int startY = (int) (dimension.height * 0.8);
        int endY = (int) (dimension.height * 0.2);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 0);

        swipe.addAction(finger.createPointerMove(java.time.Duration.ofMillis(100), PointerInput.Origin.viewport(),
                startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(java.time.Duration.ofMillis(800), PointerInput.Origin.viewport(),
                startX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
    }

    /**
     * Check if an element is displayed
     *
     * @param locator Locator for the element
     * @return True if the element is displayed, false otherwise
     */
    public static boolean isElementDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Tap on an element
     *
     * @param locator Locator for the element
     */
    public static void tapElement(By locator) {
        WebElement element = waitForElementVisible(locator);
        element.click();
    }

    /**
     * Enter text in a field
     *
     * @param locator Locator for the field
     * @param text    Text to enter
     */
    public static void enterText(By locator, String text) {
        WebElement element = waitForElementVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Perform swipe by percentages
     *
     * @param startXPercentage Start X percentage (0.0 to 1.0)
     * @param startYPercentage Start Y percentage (0.0 to 1.0)
     * @param endXPercentage   End X percentage (0.0 to 1.0)
     * @param endYPercentage   End Y percentage (0.0 to 1.0)
     */
    public static void swipeByPercentage(double startXPercentage, double startYPercentage, double endXPercentage,
            double endYPercentage) {
        Dimension size = driver.manage().window().getSize();
        int startX = (int) (size.width * startXPercentage);
        int startY = (int) (size.height * startYPercentage);
        int endX = (int) (size.width * endXPercentage);
        int endY = (int) (size.height * endYPercentage);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 0);

        swipe.addAction(finger.createPointerMove(java.time.Duration.ofMillis(0), PointerInput.Origin.viewport(), startX,
                startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(
                finger.createPointerMove(java.time.Duration.ofMillis(500), PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
    }
}
