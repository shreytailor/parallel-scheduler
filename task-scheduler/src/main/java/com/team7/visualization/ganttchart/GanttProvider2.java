package com.team7.visualization.ganttchart;

import com.team7.model.Schedule;
import com.team7.model.Task;
import com.team7.parsing.Config;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GanttProvider2 {
    private Config _config;
    private Schedule _schedule;
    private Task[] _tasks;
    private GanttComponent<Number, String> _chart;
    private ObservableList<XYChart.Series<Number, String>> processorSeries;

    public GanttProvider2(Task[] tasks, Schedule schedule, Config config) {
        _config = config;
        _tasks = tasks;

        // Creating and configuring the chart.
        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();
        _chart = new GanttComponent<>(xAxis, yAxis);

        _chart.setAnimated(false);
        _chart.setTitle("Schedule Visualization");
        _chart.setLegendVisible(false);
        _chart.setBlockHeight(18);

        // Configuring the axis.
        xAxis.setMinorTickCount(4);
        xAxis.setLabel("Time (weight)");
        yAxis.setTickLabelGap(10);
        yAxis.setLabel("Processor");

        // Registering the processors.
//        List<String> processorTitles = new ArrayList<>();  //Remove this?
        processorSeries = FXCollections.observableArrayList();
        for (int counter = 0; counter < _config.getNumOfProcessors(); counter++) {
//            processorTitles.add("Processor " + counter);  //Remove this?
            processorSeries.add(new XYChart.Series());
        }

        _chart.setData(processorSeries);

        updateSchedule(schedule);
    }

    public void updateSchedule(Schedule s) {
        _schedule = s;

        try {
            // Getting the important parts of the schedule.
            byte[] taskMap = _schedule.getTaskProcessorMap();
            int[] taskStartTime = _schedule.getTaskStartTimeMap();

            //Clear series before updating
            for (XYChart.Series se : processorSeries) {
                se.getData().clear();
            }

            for (int counter = 0; counter < _tasks.length; counter++) {
                Task currentTask = _tasks[counter];
                short uniqueId = currentTask.getUniqueID();
                if (Integer.valueOf(taskMap[uniqueId]) >= 0) {
                    String processor = String.valueOf(taskMap[uniqueId]+1);
                    int startTime = taskStartTime[uniqueId];
                    int length = currentTask.getWeight();

                    XYChart.Data<Number, String> data = new XYChart.Data(
                            startTime, processor,
                            new GanttComponent.ExtraData(length, "status-bar", currentTask.getName()));

                    XYChart.Series series = processorSeries.get(Integer.valueOf(taskMap[uniqueId]));

                    series.getData().add(data);
                }
            }
        } catch (NullPointerException exception) {
            return;
        }
    }

    public GanttComponent getSchedule() {
        return _chart;
    }
}

