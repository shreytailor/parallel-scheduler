package com.team7.parsing;

import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import com.team7.model.Edge;
import com.team7.model.Graph;
import com.team7.model.Schedule;
import com.team7.model.Task;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class helps to parse the .dot file to an internal representation of the Graph, and also
 * vice-versa.
 */
public class DOTParser {

    /**
     * This method reads a .dot file from the local computer, and then parses that file in order to
     * return a Graph object which represents the graph in the .dot file, and this will act as an
     * internal representation of the graph while the application is running.
     * @param filename the filename of the .dot file you want to parse.
     * @return Graph the internal representation of the graph.
     * @throws FileNotFoundException this is thrown if the file is not found.
     */
    public static Graph read(String filename) throws FileNotFoundException {
        List<Edge> edges = new ArrayList<>();
        Map<String, Task> tasks = new HashMap<>();

        // Parse the graph using the library, and then extract the nodes and edges.
        GraphParser parser = new GraphParser(new FileInputStream(filename));
        Map<String, GraphNode> nodeMap = parser.getNodes();
        Map<String, GraphEdge> edgeMap = parser.getEdges();

        // Create tasks from the nodes of the graph, and then add them into the data structure.
        for (GraphNode n : nodeMap.values()) {
            String name = n.getId();
            int weight = Integer.parseInt((String) n.getAttribute("Weight"));
            Task t=new Task(name, weight);
            tasks.put(name, t);
        }

        // Add information about the edges to the nodes added above.
        for (GraphEdge e : edgeMap.values()) {
            String headName = e.getNode2().getId();
            Task head = tasks.get(headName);
            String tailName = e.getNode1().getId();
            Task tail = tasks.get(tailName);
            int weight = Integer.parseInt((String) e.getAttribute("Weight"));
            Edge edge = new Edge(head,tail,weight);
            head.addIngoingEdge(edge);
            tail.addOutgoingEdge(edge);
            edges.add(edge);
        }

        return new Graph(parser.getGraphId(), new ArrayList<>(tasks.values()), edges);
    }

    /**
     * This helper method is for taking the internal representation of the Schedule and Graph, and
     * converting those into an output .dot file for exporting purposes.
     * @param path the path to where you want to output the .dot file.
     * @param schedule the created Schedule by the application.
     * @param graph the internal representation of the Graph.
     */
    public static void write(String path, Schedule schedule, Graph graph) {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write("digraph ");
            writer.append(graph.getName()).append(" {\n");
            for (Task t : graph.getNodes()) {
                writer.append("\t").append(t.getName()).append("\t").append(" [Weight=").append(String.valueOf(t.getWeight())).append(",Start=").append(String.valueOf(schedule.getTaskStartTime(t))).append(",Processor=").append(String.valueOf(schedule.getTaskProcessor(t)+1)).append("];\n");
            }
            for (Edge e : graph.getEdges()) {
                writer.append("\t").append(e.getTail().getName()).append(" -> ").append(e.getHead().getName()).append("\t").append(" [Weight=").append(String.valueOf(e.getWeight())).append("];\n");
            }
            writer.append("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
