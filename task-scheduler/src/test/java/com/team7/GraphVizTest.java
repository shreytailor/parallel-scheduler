package com.team7;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Link.to;

// TODO: Messy Code, to be refactored later
public class GraphVizTest {
    public static void main(String[] args) throws IOException {
        try (InputStream dot = new FileInputStream("task-scheduler/src/dot-tests/large.DOT")) {
            File imageFile = new File("tmp/exk2.png");
            MutableGraph g = new Parser().read(dot);
            imageFile = Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(imageFile);
            imageFile = new File("tmp/exk2.png");
            Image image = new Image(imageFile.toURI().toString());
            System.out.println(image);
            ImageView imageView = new ImageView(image);
            // Create the actual window and display it.
            Stage stage = new Stage();
            stage.setScene(new Scene(new BorderPane(new Button("hi"))));
            stage.showAndWait();
        }


    }

}