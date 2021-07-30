package com.team7.model;

import java.util.List;

public class Graph {
    private List<Task> nodes;
    private List<Edge> edges;

    public Graph(List<Task> nodes, List<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<Task> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }
}
