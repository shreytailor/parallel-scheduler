package com.team7.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Task {
    private String name;
    private int weight;
    private List<Edge> inEdges;
    private List<Edge> outEdges;
    private byte uniqueID = -1;

    public Task(String name, int weight) {
        this.name = name;
        this.weight = weight;
        inEdges = new ArrayList<>();
        outEdges = new ArrayList<>();
    }

    public String getName() {
        return name;
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

    public byte getUniqueID() {
        return this.uniqueID;
    }

    public void setUniqueID(byte n) {
        uniqueID=n;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return name.equals(task.name) && weight == task.weight;
    }

    @Override
    public Task clone(){
        return new Task(name, weight);
    }


    @Override
    public int hashCode() {
        return Objects.hash(name, weight);
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", weight=" + weight +
                ", inEdges=" + inEdges +
                ", outEdges=" + outEdges +
                '}';
    }
}