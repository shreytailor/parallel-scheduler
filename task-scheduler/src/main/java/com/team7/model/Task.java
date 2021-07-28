package com.team7.model;

import java.util.ArrayList;
import java.util.List;

public class Task {
    private String name;
    private int weight;
    private List<Edge> inEdges;
    private List<Edge> outEdges;

    public Task(String name, int weight) {
        this.name = name;
        this.weight = weight;
        inEdges = new ArrayList<>();
        outEdges = new ArrayList<>();
    }

    public void addIngoingEdge(Edge e) {
        inEdges.add(e);
    }

    public void addOutgoingEdge(Edge e) {
        outEdges.add(e);
    }

    public List<Edge> getIngoingEdges() {
        return inEdges;
    }

    public List<Edge> getOutgoingEdges() {
        return outEdges;
    }

    public int getWeight() {
        return weight;
    }
}
