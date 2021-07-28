package com.team7.model;

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
}
