package graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graph {

    private final Map<String, Set<String>> graph;

    // Constructor to initialize the graph
    public Graph() {
        graph = new HashMap<>();
    }

    // Adds a node to the graph
    public void addNode(String node) {
        graph.putIfAbsent(node, new HashSet<>());
    }

    // Adds a directed edge from one node to another
    public void addEdge(String fromNode, String toNode) {
        graph.putIfAbsent(fromNode, new HashSet<>());
        graph.putIfAbsent(toNode, new HashSet<>());
        graph.get(fromNode).add(toNode);
    }

    // Returns all nodes in the graph
    public Set<String> getNodes() {
        return graph.keySet();
    }

    // Returns the total number of nodes in the graph
    public int getNodeCount() {
        return graph.size();
    }

    // Returns the out-degree (number of outgoing edges) of a node
    public int getOutDegree(String node) {
        return graph.containsKey(node) ? graph.get(node).size() : 0;
    }

    // Returns the adjacency list of the graph
    public Map<String, Set<String>> getGraph() {
        return graph;
    }
}
