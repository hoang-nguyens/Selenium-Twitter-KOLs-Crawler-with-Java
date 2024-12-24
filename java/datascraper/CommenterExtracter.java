package datascraper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import chromehandler.Driver;
import chromehandler.LoginManager;
import chromehandler.TabManager;
import chromehandler.WaitUtils;
import datahandler.InfoHandling;

public class CommenterExtracter {

    private static WebDriver driver;
    private static WaitUtils waitUtils;
    private static WebDriverWait wait;
    private LoginManager loginManager;
    private List<String> usernames;
    private List<String> passwords;

    // Constructor - WebDriver and WaitUtils are initialized via the Driver class
    public CommenterExtracter(List<String> usernames, List<String> passwords) {
        this.usernames = usernames;
        this.passwords = passwords;
        this.loginManager = new LoginManager(driver, waitUtils);
    }

    // Method to process data with account switching
    public void processWithAccountSwitching(
        String filePath, 
        int startIndex, 
        int totalRows, 
        int batchSize
    ) {
        int accountIndex = 0;
        int currentIndex = startIndex;

        while (currentIndex < totalRows) {
            // Get the current account credentials
            String username = usernames.get(accountIndex);
            String password = passwords.get(accountIndex);

            // Initialize login manager and login
            loginManager.login(username, password);
            driver = loginManager.driver;
            waitUtils = loginManager.waitUtils;

            System.out.println("Logged in with: " + username);

            // Calculate endIndex for the current batch
            int endIndex = Math.min(currentIndex + batchSize - 1, totalRows - 1);

            // Process data for this batch
            try {
                CommenterExtracter.processKOLsData(filePath, currentIndex, endIndex);
            } catch (Exception e) {
                System.err.println("Error processing data from index " + currentIndex + " to " + endIndex + ": " + e.getMessage());
            }

            // Logout and prepare for the next account
            loginManager.logout();
            driver.quit();
            System.out.println("Logged out of: " + username);

            // Move to the next account
            accountIndex = (accountIndex + 1) % usernames.size();

            // Update the current index for the next batch
            currentIndex += batchSize;
        }

        // Close the driver after all processing is complete
        Driver.closeDriver();
        System.out.println("Driver closed after processing all data.");
    }

    // Method to process KOLs data and save to JSON file
    public static void processKOLsData(String filePath, int startIndex, int endIndex) {
        List<String[]> rows = datahandler.FileReader.readCommentorCSV(filePath);
        File jsonFile = new File("all.json");

        JSONObject allData = new JSONObject();

        // Load or initialize JSON data
        if (jsonFile.exists()) {
            try (FileReader reader = new FileReader(jsonFile)) {
                StringBuilder content = new StringBuilder();
                int ch;
                while ((ch = reader.read()) != -1) {
                    content.append((char) ch);
                }

                // Parse existing JSON data if not empty
                if (content.length() > 0) {
                    allData = new JSONObject(content.toString());
                }
            } catch (Exception e) {
                System.err.println("Error reading or parsing all.json: " + e.getMessage());
                allData = new JSONObject(); // Initialize as empty JSONObject if parsing fails
            }
        } else {
            // If file doesn't exist, start with an empty JSON object
            allData = new JSONObject();
        }

        // Process rows from startIndex to endIndex
        for (int i = startIndex; i <= endIndex && i < rows.size(); i++) {
            try {
                String[] row = rows.get(i);
                String kolUsername = row[0].replace("\"", "").trim();
                System.out.println("Processing KOL: " + kolUsername);

                // Fetch tweets, retweets, and followers
                Map<String, Object> tweetData = findTweetsAndRetweetsWithLinks("https://x.com/" + kolUsername);

                // Extract follower count as a string
                String followers = (String) tweetData.get("num_followers");

                // Maps to store processed data for this KOL
                Map<String, String> repostOwnerMap = new HashMap<>();
                Map<String, List<String>> tweetCommentsMap = new HashMap<>();
                Map<String, List<String>> retweetCommentsMap = new HashMap<>();

                // Process retweets
                List<String> retweets = (List<String>) tweetData.get("retweets");
                for (int j = 0; j < Math.min(retweets.size(), 5); j++) {
                    String retweetLink = retweets.get(j);
                    String retweetId = InfoHandling.extractPostId(retweetLink); // Extract post ID
                    Map<String, Set<String>> retweetComments = findTweetsAndRetweetsComment(retweetLink);

                    // Only add post owners who are not the KOL
                    Set<String> postOwners = retweetComments.get("PostOwner");
                    for (String owner : postOwners) {
                        if (!owner.equals(kolUsername)) {
                            repostOwnerMap.put(retweetId, owner);
                        }
                    }

                    // Add comments for this retweet
                    Set<String> comments = retweetComments.get("UserComments");
                    retweetCommentsMap.put(retweetId, new ArrayList<>(comments));
                }

                // Process tweets
                List<String> tweets = (List<String>) tweetData.get("tweets");
                for (int j = 0; j < Math.min(tweets.size(), 5); j++) {
                    String tweetLink = tweets.get(j);
                    String tweetId = InfoHandling.extractPostId(tweetLink); // Extract post ID
                    Map<String, Set<String>> tweetComments = findTweetsAndRetweetsComment(tweetLink);

                    // Add comments for this tweet
                    Set<String> comments = tweetComments.get("UserComments");
                    tweetCommentsMap.put(tweetId, new ArrayList<>(comments));
                }

                // Create JSON object for this KOL
                JSONObject kolData = new JSONObject();
                kolData.put("num_followers", followers); // Add follower count
                kolData.put("repostOwner", repostOwnerMap);
                kolData.put("tweetComments", tweetCommentsMap);
                kolData.put("retweetComments", retweetCommentsMap);

                // Add to main JSON object
                allData.put(kolUsername, kolData);

            } catch (Exception e) {
                System.err.println("Error processing KOL at row " + i + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Write updated JSON data to the file
        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(allData.toString(4));
            System.out.println("Data successfully written to all.json");
        } catch (IOException e) {
            System.err.println("Error writing to all.json: " + e.getMessage());
        }
    }

    // Method to find tweet and retweet comments for a given link
    public static Map<String, Set<String>> findTweetsAndRetweetsComment(String link) {
        TabManager.driver = driver;
        TabManager.openTab(link);

        Set<String> userNamesSet = new HashSet<>();
        String postOwner = null;
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        for (int i = 0; i < 1; i++) {
            waitUtils.waitForVisibilityOfElement(By.cssSelector("[data-testid='User-Name']"));

            List<WebElement> usersComment = driver.findElements(By.cssSelector("[data-testid='User-Name']"));

            for (WebElement user : usersComment) {
                try {
                    // Debugging: Print user element to inspect structure
                    System.out.println(user.getAttribute("innerHTML"));

                    // Wait for the <a> tag inside the user element to be visible
                    WebElement linkElement = user.findElement(By.cssSelector("a[href*='/']"));

                    // Ensure that the link element is visible
                    wait.until(ExpectedConditions.visibilityOf(linkElement));

                    String linkUser = linkElement.getAttribute("href");
                    String username = linkUser.substring(linkUser.lastIndexOf("/") + 1);

                    // If it's the first loop (i == 0) and postOwner is not set, save the post owner's username
                    if (i == 0 && postOwner == null) {
                        postOwner = username;
                        System.out.println("Post owner: " + postOwner);
                    }

                    // Only add the username to the set if it's not the post owner
                    if (!username.equals(postOwner)) {
                        userNamesSet.add(username);
                    }

                } catch (Exception e) {
                    System.out.println("Error retrieving username: " + e.getMessage());
                } finally {
                    // Close all tabs except the original one to save memory
                    TabManager.closeTabsExceptOriginal();
                }
            }

            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Create a Map to return both postOwner and user comments
        Map<String, Set<String>> result = new HashMap<>();

        // Add post owner first
        if (postOwner != null) {
            Set<String> postOwnerSet = new HashSet<>();
            postOwnerSet.add(postOwner);
            result.put("PostOwner", postOwnerSet);
        }

        // Add all user comments (non-post-owners)
        result.put("UserComments", userNamesSet);

        return result;
    }

    // Method to find tweets and retweets with their associated links and followers count
    public static Map<String, Object> findTweetsAndRetweetsWithLinks(String link) {
        TabManager.driver = driver;
        TabManager.openTab(link);

        Map<String, Object> tweetData = new HashMap<>();

        try {
            // Wait for tweets to load
            waitUtils.waitForVisibilityOfElement(By.cssSelector("[data-testid='tweet']"));

            // Fetch tweet links
            List<WebElement> tweetElements = driver.findElements(By.cssSelector("a[href*='/status/']"));
            List<String> tweetLinks = new ArrayList<>();
            for (WebElement tweetElement : tweetElements) {
                String tweetLink = tweetElement.getAttribute("href");
                tweetLinks.add(tweetLink);
            }

            // Fetch retweet links
            List<WebElement> retweetElements = driver.findElements(By.cssSelector("a[href*='/retweet/']"));
            List<String> retweetLinks = new ArrayList<>();
            for (WebElement retweetElement : retweetElements) {
                String retweetLink = retweetElement.getAttribute("href");
                retweetLinks.add(retweetLink);
            }

            // Fetch follower count
            WebElement followerElement = driver.findElement(By.xpath("//span[text()='Followers']"));
            String followers = followerElement.getText();

            // Add data to map
            tweetData.put("tweets", tweetLinks);
            tweetData.put("retweets", retweetLinks);
            tweetData.put("num_followers", followers);

        } catch (Exception e) {
            System.err.println("Error retrieving tweets, retweets, or followers count: " + e.getMessage());
        }

        return tweetData;
    }
}
