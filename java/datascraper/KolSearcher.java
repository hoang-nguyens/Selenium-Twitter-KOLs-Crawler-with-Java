package datascraper;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import chromehandler.Driver;
import chromehandler.LoginManager;
import chromehandler.WaitUtils;
import datahandler.FileWriters;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KolSearcher {

    private static WebDriver driver;
    private static WaitUtils waitUtils;
    private LoginManager loginManager;
    private FileWriters fileWriter;
    private List<String> usernames;
    private List<String> passwords;
    private List<String> keywords;

    // Constructor to initialize the scraper with account details and keywords
    public KolSearcher(List<String> usernames, List<String> passwords, List<String> keywords) {
        this.usernames = usernames;
        this.passwords = passwords;
        this.keywords = keywords;
        this.loginManager = new LoginManager(driver, waitUtils);
        this.fileWriter = new FileWriters();
    }

    // Method to login, scrape, and switch accounts for each keyword
    public void loginAndScrape(int numLoops) {
        int accountIndex = 0;

        for (String keyword : keywords) {
            String username = usernames.get(accountIndex);
            String password = passwords.get(accountIndex);

            loginManager.login(username, password);
            driver = loginManager.driver;
            waitUtils = loginManager.waitUtils;

            System.out.println("Logged in with: " + username);

            // Scrape users for the current keyword
            scrapeUsersFromHashtags(List.of(keyword), numLoops);

            // Logout
            loginManager.logout();
            driver.quit();
            System.out.println("Logged out of: " + username);

            // Switch to the next account
            accountIndex = (accountIndex + 1) % usernames.size();
        }

        // Close the driver after all scraping is complete
        Driver.closeDriver();
        System.out.println("Driver closed after completing all keywords.");
    }

    // Method to scrape users for a list of hashtags
    private void scrapeUsersFromHashtags(List<String> hashtags, int numLoops) {
        for (String keyword : hashtags) {
            String hashtag = "#" + keyword;
            scrapeUsers(hashtag, keyword, numLoops);
        }
    }

    // Method to search, filter, and scrape users for a single hashtag
    private void scrapeUsers(String hashtag, String keyword, int numLoops) {
        try {
            searchAndFilterByHashtag(hashtag, keyword);

            // Scrape user links
            Set<String> userLinks = getUserLinks(numLoops);

            // Write to a file
            fileWriter.appendLinksToCSV(userLinks, keyword);
        } catch (Exception e) {
            System.err.println("Error during scraping for hashtag: " + hashtag);
            e.printStackTrace();
        }
    }

    // Method to search for a hashtag and filter by 'People'
    private void searchAndFilterByHashtag(String hashtag, String keyword) {
        WebElement searchField = waitUtils.waitForVisibilityOfElement(By.cssSelector("[placeholder='Search']"));
        searchField.sendKeys(hashtag);
        searchField.submit();

        WebElement people = waitUtils.waitForVisibilityOfElement(
                By.cssSelector("a[href='/search?q=%23" + keyword + "&src=typed_query&f=user']"));
        people.click();
    }

    // Method to scroll and collect user links
    private Set<String> getUserLinks(int numLoops) {
        Set<String> userLinks = new HashSet<>();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        for (int i = 0; i < numLoops; i++) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            waitUtils.waitForVisibilityOfElement(By.cssSelector("[data-testid='UserCell'][role='button']"));
            List<WebElement> users = driver.findElements(By.cssSelector("[data-testid='UserCell'][role='button']"));

            for (WebElement user : users) {
                String href = user.findElement(By.tagName("a")).getAttribute("href");
                userLinks.add(href);
                System.out.println(href);
            }

            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        }

        return userLinks;
    }
}
