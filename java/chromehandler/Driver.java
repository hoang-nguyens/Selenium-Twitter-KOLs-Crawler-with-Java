package chromehandler;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class Driver {

    private static WebDriver driver; 
    private static WebDriverWait wait;

    // Private constructor to prevent instantiation
    private Driver() {}

    // Initialize WebDriver and WebDriverWait
    public static void initializeDriver() {
        if (driver == null) { 
            driver = new ChromeDriver(); 
            wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        }
    }

    // Get WebDriver instance
    public static WebDriver getDriver() {
        if (driver == null) {
            initializeDriver();
        }
        return driver;
    }

    // Get a new WebDriver instance
    public static WebDriver getNewDriver() {
        return new ChromeDriver();
    }

    // Get a new WebDriverWait instance
    public static WebDriverWait getNewWait() {
        return new WebDriverWait(Driver.getNewDriver(), Duration.ofSeconds(10));
    }

    // Get WebDriverWait instance
    public static WebDriverWait getWait() {
        if (wait == null) {
            initializeDriver();
        }
        return wait;
    }

    // Close the WebDriver instance
    public static void closeDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
            wait = null;
        }
    }
}
