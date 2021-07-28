package com.team7.model;

public class Edge {
    private Node head;
    private Node tail;
    private int edgeWeight;

    public Edge(Node head, Node tail, int weight) {
        this.head=head;
        this.tail=tail;
        this.edgeWeight=weight;
    }
}
