package com.team7.model;

import java.util.List;

public class Graph {
    private String name;
    private List<Task> nodes;
    private List<Edge> edges;

    public Graph(String name, List<Task> nodes, List<Edge> edges) {
        this.name = name;
        this.nodes = nodes;
        this.edges = edges;
    }

    public Graph(List<Task> nodes, List<Edge> edges) {
        this.name = "";
        this.nodes = nodes;
        this.edges = edges;
    }

    public String getName() {
        return name;
    }

    public List<Task> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }
}
