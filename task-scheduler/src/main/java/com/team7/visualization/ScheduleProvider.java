package com.team7.visualization;

import com.team7.model.Schedule;
import com.team7.parsing.Config;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class ScheduleProvider {
    private Config _config;
    private Schedule _schedule;

    public ScheduleProvider(Schedule schedule, Config config) {
        _config = config;
        _schedule = schedule;
    }

    public ScheduleRepresentation GetSchedule() {
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
        return chart;
    }
}
