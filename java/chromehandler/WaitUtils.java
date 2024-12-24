package chromehandler;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class WaitUtils {

    private WebDriverWait wait; // WebDriverWait for managing explicit waits

    // Constructor to initialize WebDriver and WebDriverWait
    public WaitUtils(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Method to wait for an element to be visible
    public WebElement waitForVisibilityOfElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // Method to wait for an element to be clickable
    public WebElement waitForElementToBeClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    // Method to wait for an element to be present in the DOM
    public WebElement waitForPresenceOfElement(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    // Method to wait for a certain condition, such as page title
    public boolean waitForTitle(String title) {
        return wait.until(ExpectedConditions.titleIs(title));
    }

    // Method to wait for an element's text to change
    public WebElement waitForTextToBePresentInElement(By locator, String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }
}
