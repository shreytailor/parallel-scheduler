package com.team7.visualization.ganttchart;

import com.team7.model.Schedule;
import com.team7.model.Task;
import com.team7.parsing.Config;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GanttProvider {
    private Config _config;
    private Schedule _schedule;

    public GanttProvider(Schedule schedule, Config config) {
        _config = config;
        _schedule = schedule;
    }

    public GanttComponent getSchedule() {

        // Creating and configuring the chart.
        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();
        final GanttComponent<Number, String> chart = new GanttComponent<>(xAxis, yAxis);
        chart.setLegendVisible(true);
        chart.setBlockHeight(15);

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
        Map<Task, Integer> taskMap = _schedule.getTaskProcessorMap();
        Map<Task, Integer> taskStartTime = _schedule.getTaskStartTimeMap();
        Iterator iterator = taskMap.entrySet().iterator();

        // Inserting each task into the graph, by iterating through them.
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            Task task = (Task) pair.getKey();

            String machine = String.valueOf(pair.getValue());
            int startTime = taskStartTime.get(pair.getKey());
            int length = task.getWeight();

            XYChart.Series series = processorSeries.get((int) pair.getValue());
            XYChart.Data<Number, String> data = new XYChart.Data(
                    startTime, machine,
                    new GanttComponent.ExtraData(length, "status-grey", task.getName()));

            series.getData().add(data);
        }

        // Adding each series to the final graph.
        for (int counter = 0; counter < processorSeries.size(); counter++) {
            chart.getData().add(counter, processorSeries.get(counter));
        }

        // Setting properties for the primary stage, and showing it.
        chart.getStylesheets().add(getClass().getResource("/stylesheets/Gantt.css").toExternalForm());
        return chart;
    }
}
