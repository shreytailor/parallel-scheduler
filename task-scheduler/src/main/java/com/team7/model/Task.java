package com.team7.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return name.equals(task.name) &&
                weight == task.weight &&
                inEdges.equals(task.inEdges) &&
                outEdges.equals(task.outEdges);
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

    /**
     * removed, since it causes recursive call with Edge class
     */
//    @Override
//    public int hashCode() {
//        return Objects.hash(name, weight, inEdges, outEdges);
//    }

}
