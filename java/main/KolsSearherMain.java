package main;

import java.util.List;

import datahandler.Config;
import datascraper.KolSearcher;

public class KolsSearherMain {
	
	public static void main(String[] args) {
		List<String> usernames = Config.USERNAMES;
        List<String> passwords = Config.PASSWORDS;
        List<String> keywords = Config.KEYWORDS;
 
		int numLoops = 5;

		// Initialize the HashtagScraperManager with test data
		KolSearcher scraperManager = new KolSearcher(usernames, passwords, keywords);

		    // Start the login and scraping process
		scraperManager.loginAndScrape(numLoops);
	}
}
