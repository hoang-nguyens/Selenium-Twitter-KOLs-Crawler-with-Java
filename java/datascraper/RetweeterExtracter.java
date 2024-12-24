package datascraper;

import chromehandler.Driver;
import chromehandler.LoginManager;
import chromehandler.TabManager;
import chromehandler.WaitUtils;
import datahandler.FileWriters;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class RetweeterExtracter {

    private static WebDriver driver;
    private static WaitUtils waitUtils;
    private LoginManager loginManager;
    private FileWriters fileWriters;

    private List<String> usernames;
    private List<String> passwords;

    public RetweeterExtracter(List<String> usernames, List<String> passwords) {
        this.usernames = usernames;
        this.passwords = passwords;
        this.fileWriters = new FileWriters();
        this.loginManager = new LoginManager(driver, waitUtils);
    }

    public void processRepostsWithAccountSwitching(String filePath, String outputPath) {
        try {
            Map<String, List<String>> kolPostMap = getKolsWithPostIds(filePath);
            int kolCount = 0;
            int accountIndex = 0;

            for (String kol : kolPostMap.keySet()) {
            	if (kolCount % 8 == 0) {
                    if (driver != null) {
                        loginManager.logout();
                    }

                    String username = usernames.get(accountIndex);
                    String password = passwords.get(accountIndex);

                    loginManager.login(username, password);
                    driver = loginManager.driver;
                    waitUtils = loginManager.waitUtils;
                    TabManager.driver = driver;

                    accountIndex = (accountIndex + 1) % usernames.size();
                }

                for (String postId : kolPostMap.get(kol)) {
                    String link = "https://x.com/" + kol + "/status/" + postId + "/retweets";

                    try {
                        Set<String> repostUsers = searchRepost(link);
                        if (!repostUsers.isEmpty()) {
                            fileWriters.saveUsersToJson(repostUsers, kol, postId, outputPath);
                        } else {
                            System.out.println("DEBUG: No users found for link: " + link);
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing post ID: " + postId + " for KOL: " + kol);
                        e.printStackTrace();
                    }

                    TabManager.closeTabsExceptOriginal();
                }

                kolCount++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Driver.closeDriver();
        }
    }

    public Set<String> searchRepost(String link) {
        Set<String> users = new HashSet<>();
        try {
            TabManager.openTab(link);

            JavascriptExecutor js = (JavascriptExecutor) driver;
            for (int i = 0; i < 1; i++) {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(2000);
            }

            List<WebElement> elements = driver.findElements(By.cssSelector("[class*=\"css-146c3p1\"]"));
            System.out.println("DEBUG: Elements found: " + (elements.size() > 0));

            for (WebElement element : elements) {
                String username = element.getText();
                if (username.startsWith("@")) {
                    users.add(username.substring(1));
                    System.out.println("DEBUG: Extracted username: " + username);
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to extract repost users.");
            e.printStackTrace();
        }
        return users;
    }

    public static Map<String, List<String>> getKolsWithPostIds(String filePath) {
        Map<String, List<String>> kolPostMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            JSONObject jsonObject = new JSONObject(jsonContent.toString());
            for (String kol : jsonObject.keySet()) {
                JSONObject kolDetails = jsonObject.getJSONObject(kol);
                Set<String> postIds = new HashSet<>();
                if (kolDetails.has("tweetComments")) {
                    postIds.addAll(kolDetails.getJSONObject("tweetComments").keySet());
                }
                kolPostMap.put(kol, new ArrayList<>(postIds));
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to read KOLs and Post IDs.");
            e.printStackTrace();
        }
        return kolPostMap;
    }
}
