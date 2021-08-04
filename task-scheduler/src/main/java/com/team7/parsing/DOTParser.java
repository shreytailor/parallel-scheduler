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

public class DOTParser {
    public static Graph read(String filename) throws FileNotFoundException {
        List<Edge> edges = new ArrayList<>();
        Map<String, Task> tasks = new HashMap<>();
        GraphParser parser = new GraphParser(new FileInputStream(filename));
        Map<String, GraphNode> nodeMap = parser.getNodes();
        Map<String, GraphEdge> edgeMap = parser.getEdges();
        Task.resetID();

        for (GraphNode n : nodeMap.values()) {
            String name = n.getId();
            int weight = Integer.parseInt((String) n.getAttribute("Weight"));
            Task t=new Task(name, weight);
            tasks.put(name, t);
        }

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

        return new Graph(new ArrayList<>(tasks.values()), edges);
    }

    public static void write(String path, Schedule schedule, Graph graph) {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write("digraph output {\n");
            for (Task t : graph.getNodes()) {
                writer.append(t.getName()).append(" [Weight=").append(String.valueOf(t.getWeight())).append(",Start=").append(String.valueOf(schedule.getTaskStartTime(t))).append(",Processor=").append(String.valueOf(schedule.getTaskProcessor(t))).append("];\n");
            }
            for (Edge e : graph.getEdges()) {
                writer.append(e.getTail().getName()).append(" -> ").append(e.getHead().getName()).append(" [Weight=").append(String.valueOf(e.getWeight())).append("];\n");
            }
            writer.append("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
