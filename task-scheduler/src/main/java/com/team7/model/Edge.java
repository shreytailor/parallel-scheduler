package com.team7.model;

import java.util.Objects;

public class Edge {
    private Task head;
    private Task tail;
    private int edgeWeight;

    public Edge(Task head, Task tail, int weight) {
        this.head=head;
        this.tail=tail;
        this.edgeWeight=weight;
    }

    public Task getTail() {
        return tail;
    }

    public Task getHead() {
        return head;
    }

    public int getWeight() {
        return edgeWeight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return head.equals(edge.head) &&
                tail.equals(edge.tail) &&
                (edgeWeight == edge.edgeWeight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, tail, edgeWeight);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "head=" + head.getName() +
                ", tail=" + tail.getName() +
                ", edgeWeight=" + edgeWeight +
                '}';
    }
}
