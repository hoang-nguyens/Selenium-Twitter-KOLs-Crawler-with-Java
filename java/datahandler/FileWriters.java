package datahandler;

import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

public class FileWriters {

    // Method to append links to a CSV file in the 'userlink' folder
    public void appendLinksToCSV(Set<String> listLink, String keyword) {
        // Create the 'userlink' folder if it doesn't exist
        File directory = new File("userlink");
        if (!directory.exists()) {
            directory.mkdir();
        }

        // Set the CSV file name based on the keyword
        String fileName = "userlink/" + keyword + ".csv";

        try (CSVWriter writer = new CSVWriter(new FileWriter(fileName, true))) { // Set to true for append mode
            for (String element : listLink) {
                writer.writeNext(new String[]{element});
            }
            System.out.println("Set appended to " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    // Write the results (followers, verified followers, following) to a CSV file
    public static void writeResultsToCSV(String filePath, String link, List<String> followers, 
                                         List<String> verifiedFollowers, List<String> following) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            // Extract username from the link (e.g., "https://x.com/Bitcoin" -> "Bitcoin")
            String username = link.substring(link.lastIndexOf("/") + 1);

            // Prepare the CSV row with the required columns
            String row = String.format("\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    username, // KOLs column now contains just the username
                    String.join(", ", followers),
                    String.join(", ", verifiedFollowers),
                    String.join(", ", following));

            // Write the row to the CSV file
            bw.write(row);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save ranks to a CSV file, sorted in descending order
    public static void saveRanksToCSV(String filePath, Map<String, Double> ranks) {
        List<Map.Entry<String, Double>> sortedRanks = ranks.entrySet()
                .stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .collect(Collectors.toList());

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            // Write the header
            bw.write("Rank,Node,PageRank\n");

            // Write the sorted ranks to the file
            int rank = 1;
            for (Map.Entry<String, Double> entry : sortedRanks) {
                bw.write(rank + "," + entry.getKey() + "," + entry.getValue() + "\n");
                rank++;
            }

            System.out.println("Ranks successfully saved to " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    // Write the given JSON array to a JSON file
    public static void writeToJSONFile(String filePath, JSONArray jsonArray) {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(jsonArray.toString(4));
            System.out.println("Data successfully saved to " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing to JSON file: " + filePath);
            e.printStackTrace();
        }
    }

    // Save result to a JSON file with pretty print
    public static void saveResultToJson(Object result, String fileName) {
        try (FileWriter file = new FileWriter(fileName)) {
            JSONObject jsonObject = new JSONObject(result);
            file.write(jsonObject.toString(4));
            System.out.println("Results saved to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Saves a set of users to a JSON file under the given KOL and post ID structure.
     *
     * @param users    Set of usernames to save.
     * @param kol      The KOL (Key Opinion Leader) username.
     * @param postId   The ID of the post associated with the KOL.
     * @param filePath Path to the JSON file.
     */
    public void saveUsersToJson(Set<String> users, String kol, String postId, String filePath) {
        try {
            // Initialize the JSON object to store data
            JSONObject jsonObject = new JSONObject();

            File file = new File(filePath);
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new java.io.FileReader(file));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                reader.close();

                // Attempt to parse the existing file content into a JSON object
                if (content.length() > 0) {
                    try {
                        jsonObject = new JSONObject(content.toString());
                    } catch (org.json.JSONException e) {
                        System.err.println("WARNING: File content is not valid JSON. Overwriting with new data.");
                    }
                }
            }

            // Ensure the KOL object exists in the JSON structure
            if (!jsonObject.has(kol)) {
                jsonObject.put(kol, new JSONObject());
            }

            // Add the postId and associated users to the KOL object
            JSONObject kolObject = jsonObject.getJSONObject(kol);
            JSONArray userArray = new JSONArray(users); // Convert Set<String> to JSONArray
            kolObject.put(postId, userArray);

            // Save the JSON object back to the file
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(jsonObject.toString(4)); // Pretty-print JSON with 4 spaces
            fileWriter.flush();
            fileWriter.close();

            System.out.println("Results saved to " + filePath);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to save results to JSON.");
            e.printStackTrace();
        }
    }
}
