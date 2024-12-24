package graph;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GraphVisualizer {
    private final Map<String, Set<String>> graph;

    // Constructor to initialize GraphVisualizer with the graph manager
    public GraphVisualizer(Graph graphManager) {
        this.graph = graphManager.getGraph();
    }

    // Method to visualize the graph
    public void visualize() {
        mxGraph mxGraph = new mxGraph();
        Object parent = mxGraph.getDefaultParent();

        // Start the update process for the graph model
        mxGraph.getModel().beginUpdate();
        try {
            // Create a map for the vertices to manage node relationships
            Map<String, Object> vertexMap = new HashMap<>();

            // Add vertices to the graph for each node
            for (String node : graph.keySet()) {
                Object vertex = mxGraph.insertVertex(parent, null, node, 0, 0, 80, 30);
                vertexMap.put(node, vertex);
            }

            // Add edges between connected nodes
            for (String node : graph.keySet()) {
                for (String connectedNode : graph.get(node)) {
                    mxGraph.insertEdge(parent, null, "", vertexMap.get(node), vertexMap.get(connectedNode));
                }
            }
        } finally {
            mxGraph.getModel().endUpdate();
        }

        // Layout and display settings
        mxFastOrganicLayout layout = new mxFastOrganicLayout(mxGraph);
        layout.execute(mxGraph.getDefaultParent());

        // Create a JFrame to display the graph visualization
        JFrame frame = new JFrame("Graph Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new mxGraphComponent(mxGraph));
        frame.setSize(1200, 900); 
        frame.setVisible(true);
    }
}
