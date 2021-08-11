package com.team7.visualization;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Link.to;

public class VisualizationInputGraph {
    public static void main(String[] args) throws IOException{
        try (InputStream dot = VisualizationInputGraph.class.getResourceAsStream("")) {
            MutableGraph g = new Parser().read(dot);
            Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(new File("tmp/tmp.png"));
        }
    }
}