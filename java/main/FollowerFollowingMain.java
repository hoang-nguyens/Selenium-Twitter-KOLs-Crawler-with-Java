package main;

import java.util.List;

import datahandler.Config;
import datascraper.FollowerFollowingTracker;

public class FollowerFollowingMain {
	public static void main(String[] args) {
        // Initialize the list of usernames and passwords
		List<String> usernames = Config.USERNAMES;
        List<String> passwords = Config.PASSWORDS;

        // Specify the path to the CSV file containing the links
        String csvFilePath = "userlink/blockchain.csv"; 

        // Create an instance of FindFollowersAndSwitchAccount
        FollowerFollowingTracker findFollowersAndSwitchAccount = new FollowerFollowingTracker(usernames, passwords);

        
        int numLoops = 2; 
        findFollowersAndSwitchAccount.switchAccountAfterProcessingUsers(csvFilePath, numLoops);

        // Optional: Print message to confirm completion
        System.out.println("Completed scraping all links and switched accounts.");
    }
}
