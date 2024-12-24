package main;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

import graph.Graph;
import graph.GraphBuilder;
import graph.PageRank;
import datahandler.FileReader;
import datahandler.FileWriters;

public class GraphMain {
    public static void main(String[] args) {
        try {
            // Initialize components
            FileReader fileReader = new FileReader();
            Graph graph = new Graph();
            GraphBuilder graphBuilder = new GraphBuilder(graph);

            // File path for JSON
            String jsonFile = "data.json";  // JSON file to build the graph

            // Read JSON and build the graph
            JSONObject jsonObject = fileReader.readJSON(jsonFile);
            graphBuilder.buildGraphFromJSON(jsonObject);

            // Step 3: Compute PageRank
            PageRank pageRank = new PageRank(graph);
            Map<String, Double> pageRankScores = pageRank.calculatePageRank();

            // Step 4: Filter and save PageRank scores of KOLs (top-level keys in the JSON)
            Map<String, Double> kolRanks = getKOLRanks(jsonObject, pageRankScores);

            // Step 5: Save KOL ranks to CSV
            FileWriters.saveRanksToCSV("kol_ranks.csv", kolRanks);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Extracts PageRank scores for KOLs present in the provided JSON object.
     *
     * @param jsonObject     The JSON object containing the KOL data.
     * @param pageRankScores Map containing the PageRank scores of all nodes.
     * @return A map with KOL usernames and their respective PageRank scores.
     */
    private static Map<String, Double> getKOLRanks(JSONObject jsonObject, Map<String, Double> pageRankScores) {
        Map<String, Double> kolRanks = new HashMap<>();

        // Iterate through the JSON keys (KOL usernames)
        for (String kol : jsonObject.keySet()) {
            if (pageRankScores.containsKey(kol)) {
                kolRanks.put(kol, pageRankScores.get(kol));
            } else {
                kolRanks.put(kol, 0.0);  // If KOL not found in the PageRank, assign 0.0
            }
        }

        return kolRanks;
    }
}
