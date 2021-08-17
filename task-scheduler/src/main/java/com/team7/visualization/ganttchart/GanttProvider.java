package com.team7.visualization.ganttchart;

import com.team7.algorithm.Scheduler;
import com.team7.model.Schedule;
import com.team7.model.Task;
import com.team7.parsing.Config;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GanttProvider {
    private Config _config;
    private Schedule _schedule;
    private Task[] _tasks;

    public GanttProvider(Task[] tasks, Schedule schedule, Config config) {
        _config = config;
        _schedule = schedule;
        _tasks = tasks;
    }

    public GanttComponent getSchedule() {

        // Creating and configuring the chart.
        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();
        final GanttComponent<Number, String> chart = new GanttComponent<>(xAxis, yAxis);
        chart.setTitle("Schedule Visualization");
        chart.setLegendVisible(false);
        chart.setBlockHeight(18);

        // Configuring the axis.
        xAxis.setMinorTickCount(4);
        xAxis.setLabel("Time (weight)");
        yAxis.setTickLabelGap(10);
        yAxis.setLabel("Processor");

        // Registering the processors.
        List<String> processorTitles = new ArrayList<>();
        List<XYChart.Series> processorSeries = new ArrayList<>();
        for (int counter = 0; counter < _config.getNumOfProcessors(); counter++) {
            processorTitles.add("Processor " + counter);
            processorSeries.add(new XYChart.Series());
        }

        // Getting the important parts of the schedule.
        byte[] taskMap = _schedule.getTaskProcessorMap();
        int[] taskStartTime = _schedule.getTaskStartTimeMap();

        for (int counter = 0; counter < _tasks.length; counter++) {
            Task currentTask = _tasks[counter];
            short uniqueId = currentTask.getUniqueID();
            String processor = String.valueOf(taskMap[uniqueId]);
            int startTime = taskStartTime[uniqueId];
            int length = currentTask.getWeight();

            XYChart.Series series = processorSeries.get(Integer.valueOf(taskMap[uniqueId]));
            XYChart.Data<Number, String> data = new XYChart.Data(
                    startTime, processor,
                    new GanttComponent.ExtraData(length, "status-bar", currentTask.getName()));

            series.getData().add(data);
        }

        // Adding each series to the final graph.
        for (int counter = 0; counter < processorSeries.size(); counter++) {
            chart.getData().add(counter, processorSeries.get(counter));
        }

        return chart;
    }
}
