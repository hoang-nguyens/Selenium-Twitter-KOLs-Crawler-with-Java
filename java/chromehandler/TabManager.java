package chromehandler;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.Set;

public class TabManager {

    public static WebDriver driver;
    
    // Default constructor
    public TabManager() {
    }

    // Constructor with custom WebDriver
    public TabManager(WebDriver driver) {
        TabManager.driver = driver;
    }

    // Open a new tab with the specified URL
    public static void openTab(String url) {
        // Open the new tab using JavascriptExecutor
        ((JavascriptExecutor) driver).executeScript("window.open('" + url + "','_blank');");

        // Get the original window handle
        String originalTab = driver.getWindowHandle();

        // Switch to the new tab
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalTab)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Copy cookies from the original tab to the new tab
        Set<Cookie> cookies = driver.manage().getCookies();
        for (Cookie cookie : cookies) {
            driver.manage().addCookie(cookie);
        }

        // Navigate to the specified URL in the new tab
        driver.navigate().to(url);
    }

    // Close all tabs except the original one
    public static void closeTabsExceptOriginal() {
        // Get the original tab and all open window handles
        String originalTab = driver.getWindowHandle();
        Set<String> allTabs = driver.getWindowHandles();

        // Close all tabs except the original one
        for (String tab : allTabs) {
            if (!tab.equals(originalTab)) {
                driver.switchTo().window(tab);
                driver.close();
            }
        }

        // Switch back to the original tab
        driver.switchTo().window(originalTab);
    }
}
