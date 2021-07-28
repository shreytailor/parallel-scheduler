package com.team7.model;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private String name;
    private int nodeWeight;
    private List<Edge> inEdges;
    private List<Edge> outEdges;

    public Node(String name, int weight) {
        this.name=name;
        this.nodeWeight=weight;
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
        return nodeWeight;
    }
}
