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
import javafx.scene.chart.CategoryAxis;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import com.team7.model.Schedule;
import com.team7.parsing.Config;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class SchedulerScreenController implements Initializable {

    private Config _config;
    private Schedule _schedule;

    public SchedulerScreenController(Schedule schedule, Config config) {
        _config = config;
        _schedule = schedule;
    }

    @FXML
    private BorderPane stateGraphContainer;

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

        // Creating and configuring the chart.
        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();
        final ScheduleRepresentation<Number, String> chart = new ScheduleRepresentation<>(xAxis, yAxis);
        chart.setLegendVisible(true);
        chart.setBlockHeight(10);

        // Configuring the axis.
        xAxis.setTickLabelFill(Color.CHOCOLATE);
        xAxis.setMinorTickCount(4);
        yAxis.setTickLabelFill(Color.CHOCOLATE);
        yAxis.setTickLabelGap(10);

        // Registering the processors.
        List<String> processorTitles = new ArrayList<>();
        List<XYChart.Series> processorSeries = new ArrayList<>();
        for (int counter = 0; counter < _config.getNumOfProcessors(); counter++) {
            processorTitles.add("Processor " + counter);
            processorSeries.add(new XYChart.Series());
        }

        // Getting the important parts of the schedule.
        List<Integer> taskProcess = new ArrayList<>(_schedule.getTaskProcessorMap().values());
        List<Integer> taskStartTime = new ArrayList(_schedule.getTaskStartTimeMap().values());
        List<Integer> taskEndTime = new ArrayList(_schedule.getTaskFinishTimeMap().values());

        // Inserting each task into the graph, by iterating through them.
        for (int counter = 0; counter < _schedule.getNumberOfTasks(); counter++) {
            String machine = processorTitles.get(taskProcess.get(counter));
            int startTime = taskStartTime.get(counter);
            int length = taskEndTime.get(counter) - startTime;
            XYChart.Series series = processorSeries.get(taskProcess.get(counter));
            series.getData().add(new XYChart.Data(taskStartTime.get(counter), machine, new ScheduleRepresentation.ExtraData(length, "status-grey")));
        }

        // Adding each series to the final graph.
        for (int counter = 0; counter < processorSeries.size(); counter++) {
            chart.getData().add(counter, processorSeries.get(counter));
        }

        // Setting properties for the primary stage, and showing it.
        chart.getStylesheets().add(getClass().getResource("/stylesheets/schedule-representation.css").toExternalForm());

        stateGraphContainer.setCenter(chart);
        TimerTask tm = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    
                });
            }
        };
}
