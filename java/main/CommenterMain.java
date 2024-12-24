package main;

import java.util.List;

import datahandler.Config; // Import the config class
import datascraper.CommenterExtracter;

public class CommenterMain {
    public static void main(String[] args) {
        List<String> usernames = Config.USERNAMES;
        List<String> passwords = Config.PASSWORDS;
        
        CommenterExtracter findCommentUsers = new CommenterExtracter(usernames, passwords);
        findCommentUsers.processWithAccountSwitching("result.csv", 0, 100, 10);
    }
}
