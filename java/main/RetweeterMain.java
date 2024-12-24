package main;

import java.util.List;

import datahandler.Config;
import datascraper.RetweeterExtracter;

public class RetweeterMain {
	public static void main(String[] args) {
    	// Step 1: Define usernames and passwords
		List<String> usernames = Config.USERNAMES;
        List<String> passwords = Config.PASSWORDS;

        // Step 2: Initialize FindRepostUsers instance
        RetweeterExtracter findRepostUsers = new RetweeterExtracter(usernames, passwords);

        // Step 3: Define the file path for KOLs and tweet IDs
        String filePath = "all.json"; // Replace with the actual file path
        String outputPath = "repost.json";

        // Step 4: Call processRepostsWithAccountSwitching method
        findRepostUsers.processRepostsWithAccountSwitching(filePath, outputPath);

        // Output should save the results to "repost.json"
        System.out.println("Processing complete. Check 'repost.json' for results.");
    }
}
