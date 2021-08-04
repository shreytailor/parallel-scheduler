package com.team7.visualization;

import com.team7.model.Schedule;
import com.team7.parsing.Config;
import com.team7.visualization.ganttchart.GanttProvider;
import com.team7.visualization.system.CPUUtilizationProvider;
import com.team7.visualization.system.RAMUtilizationProvider;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

public class SchedulerScreenController implements Initializable {

    private Config _config;
    private Schedule _schedule;
    private long time;

    public SchedulerScreenController(Schedule schedule, Config config) {
        _config = config;
        _schedule = schedule;
        time = 0;
    }

    @FXML
    private BorderPane stateGraphContainer;

    @FXML
    private Label timerLabel;

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
        cpuUtilizationProvider = new CPUUtilizationProvider(cpuUsageChart);
        cpuUtilizationProvider.startTracking();

        NumberAxis cpuYAxis = (NumberAxis) cpuUsageChart.getYAxis();
        cpuUsageChart.getXAxis().setLabel("Time (seconds)");
        cpuYAxis.setLabel("Usage (%)");
        cpuYAxis.setUpperBound(cpuUtilizationProvider.getUpperBound());

        ramUtilizationProvider = new RAMUtilizationProvider(ramUsageChart);
        ramUtilizationProvider.startTracking();

        NumberAxis ramYAxis = (NumberAxis) ramUsageChart.getYAxis();
        ramUsageChart.getXAxis().setLabel("Time (seconds)");
        ramYAxis.setLabel("Usage (%)");
        ramYAxis.setUpperBound(ramUtilizationProvider.getUpperBound());

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                time += 100;
                timerLabel.setText(Long.toString(time));
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        GanttProvider scheduleProvider = new GanttProvider(_schedule, _config);
        stateGraphContainer.setCenter(scheduleProvider.getSchedule());
    }
}
