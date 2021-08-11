package com.team7.visualization;

import com.team7.visualization.system.CPUUtilizationProvider;
import com.team7.visualization.system.RAMUtilizationProvider;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class SchedulerScreenController implements Initializable {

    @FXML
    public Button viewToggleButton;

    @FXML
    public ImageView restartButton;

    @FXML
    public ImageView themeToggleButton;

    @FXML
    public LineChart<String, Number> cpuUsageChart;

    @FXML
    public LineChart<String, Number> ramUsageChart;

    private CPUUtilizationProvider cpuUtilizationProvider;
    private RAMUtilizationProvider ramUtilizationProvider;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Registering the CPU chart to a custom provider, and starting to track data.
        cpuUtilizationProvider = new CPUUtilizationProvider(cpuUsageChart);
        cpuUtilizationProvider.startTracking();

        // Applying custom properties to the CPU chart.
        NumberAxis cpuYAxis = (NumberAxis) cpuUsageChart.getYAxis();
        cpuUsageChart.getXAxis().setLabel("Time (seconds)");
        cpuYAxis.setLabel("Usage (%)");
        cpuYAxis.setUpperBound(cpuUtilizationProvider.getUpperBound());

        // Registering the RAM chart to a custom provider, and starting to track data.
        ramUtilizationProvider = new RAMUtilizationProvider(ramUsageChart);
        ramUtilizationProvider.startTracking();

        // Applying custom properties to the RAM chart.
        NumberAxis ramYAxis = (NumberAxis) ramUsageChart.getYAxis();
        ramUsageChart.getXAxis().setLabel("Time (seconds)");
        ramYAxis.setLabel("Usage (%)");
        ramYAxis.setUpperBound(ramUtilizationProvider.getUpperBound());
    }

    // Shows the Input Image in a popup window
    public void handleViewToggleButton() throws IOException {

        // This command is useful for debugging to determine the directory
        // System.out.println(System.getProperty("user.dir"));

        try (InputStream dot = new FileInputStream("task-scheduler/src/dot-tests/large.DOT")) {

            // parse the dot file and generate an image
            File imageFile = new File("tmp/tmp.png");
            MutableGraph g = new Parser().read(dot);
            Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(imageFile);

            // convert the image file to javafx component
            Image image = new Image(imageFile.toURI().toString());
            ImageView imageView = new ImageView(image);

            // Shows the input graph in a pop up window
            Stage popupwindow = new Stage();
            popupwindow.initModality(Modality.APPLICATION_MODAL);
            popupwindow.setTitle("Input Graph");
            popupwindow.setScene(new Scene(new Pane(imageView)));
            popupwindow.showAndWait();
        }
    }
}
