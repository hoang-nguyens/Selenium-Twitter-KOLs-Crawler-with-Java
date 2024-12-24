package graph;

import org.json.JSONArray;
import org.json.JSONObject;

public class GraphBuilder {

    private final Graph graph;

    // Constructor to initialize the GraphBuilder with a Graph instance
    public GraphBuilder(Graph graph) {
        this.graph = graph;
    }

    // Processes JSON data to add vertices (nodes) and edges to the graph
    public void buildGraphFromJSON(JSONObject jsonObject) {
        for (String kol : jsonObject.keySet()) {
            JSONObject kolData = jsonObject.getJSONObject(kol);

            // Process retweet comments
            processRetweetComments(kolData, kol);

            // Process tweet comments
            processTweetComments(kolData, kol);

            // Process reposts (new part to add)
            processReposts(kolData, kol);

            // Process followers, verified followers, and followings
            processFollowers(kolData, kol);
            processVerifiedFollowers(kolData, kol);
            processFollowings(kolData, kol);
        }
    }

    // Helper method to process retweet comments and add them to the graph
    private void processRetweetComments(JSONObject kolData, String kol) {
        JSONObject retweetComments = kolData.getJSONObject("retweetComments");
        for (String tweetId : retweetComments.keySet()) {
            JSONArray commentorsArray = retweetComments.getJSONArray(tweetId);
            for (int i = 0; i < commentorsArray.length(); i++) {
                String commentor = commentorsArray.getString(i);
                graph.addNode(commentor);
                graph.addNode(kol);
                graph.addEdge(commentor, tweetId);
                graph.addEdge(kol, tweetId);
                graph.addEdge(tweetId, kol);
            }
        }
    }

    // Helper method to process tweet comments and add them to the graph
    private void processTweetComments(JSONObject kolData, String kol) {
        JSONObject tweetComments = kolData.getJSONObject("tweetComments");
        for (String tweetId : tweetComments.keySet()) {
            JSONArray commentorsArray = tweetComments.getJSONArray(tweetId);
            for (int i = 0; i < commentorsArray.length(); i++) {
                String commentor = commentorsArray.getString(i);
                graph.addNode(commentor);
                graph.addNode(kol);
                graph.addEdge(commentor, tweetId);
                graph.addEdge(tweetId, kol);
                graph.addEdge(commentor, kol);
            }
        }
    }

    // Helper method to process reposts and add them to the graph (new part)
    private void processReposts(JSONObject kolData, String kol) {
        JSONObject repostOwner = kolData.getJSONObject("repostOwner");
        for (String tweetId : repostOwner.keySet()) {
            String repostedBy = repostOwner.getString(tweetId);

            // Add the reposter to the graph if not already added
            graph.addNode(repostedBy);
            graph.addNode(kol);

            // Add edges from the reposter to the tweet, and from the tweet to the KOL
            graph.addEdge(tweetId, repostedBy);  // Reposter -> Tweet
            graph.addEdge(kol, tweetId);         // KOL -> Tweet
            graph.addEdge(tweetId, kol);         // Tweet -> KOL
            graph.addEdge(kol, repostedBy);
        }
    }

    // Helper method to process followers and add them to the graph
    private void processFollowers(JSONObject kolData, String kol) {
        JSONArray followers = kolData.getJSONArray("followers");
        for (int i = 0; i < followers.length(); i++) {
            String follower = followers.getString(i);
            graph.addNode(follower);
            graph.addEdge(follower, kol);
        }
    }

    // Helper method to process verified followers and add them to the graph
    private void processVerifiedFollowers(JSONObject kolData, String kol) {
        JSONArray verifiedFollowers = kolData.getJSONArray("verifiedFollowers");
        for (int i = 0; i < verifiedFollowers.length(); i++) {
            String verifiedFollower = verifiedFollowers.getString(i);
            graph.addNode(verifiedFollower);
            graph.addEdge(verifiedFollower, kol);
        }
    }

    // Helper method to process followings and add them to the graph
    private void processFollowings(JSONObject kolData, String kol) {
        JSONArray followings = kolData.getJSONArray("followings");
        for (int i = 0; i < followings.length(); i++) {
            String following = followings.getString(i);
            graph.addNode(following);
            graph.addEdge(kol, following);
        }
    }
}
