package com.team7.parsing;

import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import com.team7.model.Edge;
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
    private List<Edge> edges;
    private Map<String, Task> tasks;

    public DOTParser() {
        edges = new ArrayList<>();
        tasks = new HashMap<>();
    }

    public void read(String filename) throws FileNotFoundException {
        GraphParser parser = new GraphParser(new FileInputStream(filename));
        Map<String, GraphNode> nodeMap = parser.getNodes();
        Map<String, GraphEdge> edgeMap = parser.getEdges();

        for (GraphNode n : nodeMap.values()) {
            String name = n.getId();
            int weight = Integer.parseInt((String) n.getAttribute("Weight"));
            tasks.put(name, new Task(name, weight));
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

        return new ArrayList<Task>(tasks.values());
    }

    public void write(String path, Schedule schedule, List<Edge> edges) {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write("digraph output {\n");
            for (Task t : schedule.getTaskProcessorMap().keySet()) {
                writer.append(t.getName()).append(" [Weight=").append(String.valueOf(t.getWeight())).append(",Start=").append(String.valueOf(schedule.getTaskStartTime(t))).append(",Processor=").append(String.valueOf(schedule.getTaskProcessor(t))).append("];\n");
            }
            for (Edge e : edges) {
                writer.append(e.getTail().getName()).append(" -> ").append(e.getHead().getName()).append(" [Weight=").append(String.valueOf(e.getWeight())).append("];\n");
            }
            writer.append("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Map<String, Task> getTasks() {
        return tasks;
    }
}
