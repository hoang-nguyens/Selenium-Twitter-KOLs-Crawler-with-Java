package graph;

import java.util.HashMap;
import java.util.Map;

public class PageRank {

    private final Graph graph;
    private final double dampingFactor;
    private final int maxIterations;
    private final double tolerance;

    // Constructor with default parameters
    public PageRank(Graph graph) {
        this(graph, 0.85, 100, 1e-6);
    }

    // Constructor with custom parameters
    public PageRank(Graph graph, double dampingFactor, int maxIterations, double tolerance) {
        this.graph = graph;
        this.dampingFactor = dampingFactor;
        this.maxIterations = maxIterations;
        this.tolerance = tolerance;
    }

    // Calculates the PageRank for all nodes in the graph
    public Map<String, Double> calculatePageRank() {
        Map<String, Double> ranks = new HashMap<>();
        Map<String, Double> previousRanks = new HashMap<>();
        double danglingSum;

        // Initialize ranks: Initially set all nodes to equal rank
        for (String node : graph.getNodes()) {
            ranks.put(node, 1.0);
            previousRanks.put(node, 1.0 / graph.getNodeCount());
        }

        // Iteratively update ranks until convergence or max iterations
        for (int i = 0; i < maxIterations; i++) {
            danglingSum = 0.0;

            // Calculate the sum of the ranks of dangling nodes
            for (String node : graph.getNodes()) {
                if (graph.getOutDegree(node) == 0) {
                    danglingSum += previousRanks.get(node);
                }
            }

            // Update ranks for each node
            boolean converged = true; // Assume convergence until proven otherwise
            for (String node : graph.getNodes()) {
                double newRank = (1 - dampingFactor) / graph.getNodeCount();
                double sum = 0.0;

                // Sum ranks of inbound edges
                for (String neighbor : graph.getGraph().get(node)) {
                    int outDegree = graph.getOutDegree(neighbor);
                    sum += (outDegree > 0) ? previousRanks.get(neighbor) / outDegree : 0;
                }

                // Add the contribution from dangling nodes
                newRank += dampingFactor * sum + dampingFactor * (danglingSum / graph.getNodeCount());

                // Check for convergence (if the rank change is small enough)
                if (Math.abs(newRank - previousRanks.get(node)) > tolerance) {
                    converged = false;
                }

                // Update rank
                ranks.put(node, newRank);
            }

            // If all nodes have converged, break early
            if (converged) {
                break;
            }

            // Update previous ranks for next iteration
            previousRanks.putAll(ranks);
        }

        return ranks;
    }

    // Returns the rank of a specific user
    public double getRankOfUser(String user, Map<String, Double> ranks) {
        return ranks.getOrDefault(user, 0.0);
    }
}
