package com.team7.visualization;

import com.team7.visualization.system.CPUUtilizationProvider;
import com.team7.visualization.system.RAMUtilizationProvider;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
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
}
