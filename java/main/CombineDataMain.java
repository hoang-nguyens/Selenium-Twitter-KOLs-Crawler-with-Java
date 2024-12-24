package main;

import org.apache.commons.csv.CSVRecord;
import org.json.JSONObject;

import datahandler.FileReader;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class CombineDataMain {

    private FileReader fileReader;

    public CombineDataMain() {
        this.fileReader = new FileReader();
    }

 // Method to combine the data into a single JSON object
    public JSONObject combineData(String csvFile, String kolJsonFile, String repostJsonFile) throws IOException, JSONException {
        JSONObject finalData = new JSONObject();

        // Read the data from the CSV and JSON files
        List<CSVRecord> csvData = fileReader.readWholeDataCSV(csvFile);
        JSONObject kolJsonData = fileReader.readJSON(kolJsonFile);
        JSONObject repostJsonData = fileReader.readJSON(repostJsonFile);

        // Iterate through each KOL in the CSV file
        for (CSVRecord record : csvData) {
            String kol = record.get(0);  // KOL username
            String followers = record.get(1);  // Followers
            String verifiedFollowers = record.get(2);  // Verified Followers
            String following = record.get(3);  // Following

            // Initialize a new JSONObject for each KOL
            JSONObject kolData = new JSONObject();

            // Add the number of followers, verified followers, and following to the KOL's data
            kolData.put("followers", new JSONArray(followers.split(", ")));
            kolData.put("verifiedFollowers", new JSONArray(verifiedFollowers.split(", ")));
            kolData.put("followings", new JSONArray(following.split(", ")));

            // Extract repostOwner and retweetComments (reposters) from the KOL JSON data
            JSONObject repostOwnerData = new JSONObject();
            JSONObject tweetCommentsData = new JSONObject();
            if (kolJsonData.has(kol)) {
                JSONObject kolJson = kolJsonData.getJSONObject(kol);
                if (kolJson.has("repostOwner")) {
                    repostOwnerData = kolJson.getJSONObject("repostOwner");
                }
                if (kolJson.has("retweetComments")) {
                    tweetCommentsData = kolJson.getJSONObject("retweetComments");
                }

                // **Extract num_followers from kolJsonData**
                if (kolJson.has("num_followers")) {
                    kolData.put("num_followers", kolJson.getInt("num_followers"));
                }
            }

            // Add repostOwner and retweetComments (reposters) to KOL's data
            kolData.put("repostOwner", repostOwnerData);
            kolData.put("retweetComments", tweetCommentsData);

            // Extract tweet comments (commenters)
            JSONObject tweetComments = new JSONObject();
            if (kolJsonData.has(kol)) {
                JSONObject kolJson = kolJsonData.getJSONObject(kol);
                if (kolJson.has("tweetComments")) {
                    JSONObject tweetCommentsJSON = kolJson.getJSONObject("tweetComments");
                    for (String tweetId : tweetCommentsJSON.keySet()) {
                        JSONArray commenters = tweetCommentsJSON.getJSONArray(tweetId);
                        tweetComments.put(tweetId, commenters);
                    }
                }
            }
            kolData.put("tweetComments", tweetComments);

            // Add reposters data (from repostJsonData) in a similar structure to tweet comments
            JSONObject repostersData = new JSONObject();
            if (repostJsonData.has(kol)) {
                JSONObject repostJson = repostJsonData.getJSONObject(kol);
                for (String tweetId : repostJson.keySet()) {
                    Object repostData = repostJson.get(tweetId);

                    if (repostData instanceof JSONArray) {
                        // If repostData is a JSONArray, add it to repostersData
                        JSONArray reposters = (JSONArray) repostData;
                        repostersData.put(tweetId, reposters);
                    } else if (repostData instanceof JSONObject) {
                        // If repostData is a JSONObject, handle it accordingly
                        JSONObject reposters = (JSONObject) repostData;
                        repostersData.put(tweetId, reposters);
                    }
                }
            }
            kolData.put("reposters", repostersData);

            // Add the KOL data to the final JSON object
            finalData.put(kol, kolData);
        }

        return finalData;
    }



    // Method to write the combined data into a JSON file
    public void saveToJSON(JSONObject combinedData, String outputFilePath) {
        try {
            java.nio.file.Files.write(java.nio.file.Paths.get(outputFilePath), combinedData.toString(4).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Example usage
    public static void main(String[] args) throws IOException, JSONException {
        CombineDataMain dataCombiner = new CombineDataMain();

        // Define the paths to your CSV and JSON files
        String csvFile = "result.csv";
        String kolJsonFile = "all.json";
        String repostJsonFile = "repost.json";
        String outputFilePath = "data.json";

        // Combine the data
        JSONObject combinedData = dataCombiner.combineData(csvFile, kolJsonFile, repostJsonFile);

        // Save the combined data into a JSON file
        dataCombiner.saveToJSON(combinedData, outputFilePath);

        // Optionally print the combined data
        System.out.println(combinedData.toString(4));  // Pretty print with indentation of 4
    }
}
