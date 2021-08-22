package com.team7.visualization.ganttchart;

import com.team7.model.Schedule;
import com.team7.model.Task;
import com.team7.parsing.Config;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class GanttProvider {
    private Config config;
    private Schedule schedule;
    private Task[] tasks;
    private GanttComponent<Number, String> chart;
    private ObservableList<XYChart.Series<Number, String>> processorSeries;

    /**
     * This is the lone constructor for creating an instance for this class.
     * @param tasks a task array representing the tasks which we want to show on the Gantt chart.
     * @param schedule a Schedule instance representing the created schedule.
     * @param config a Config instance representing the CLI arguments passed by the user.
     */
    public GanttProvider(Task[] tasks, Schedule schedule, Config config) {
        this.config = config;
        this.tasks = tasks;

        // Creating and configuring the chart.
        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();
        chart = new GanttComponent<>(xAxis, yAxis);

        chart.setAnimated(false);
        chart.setTitle("Schedule Visualization");
        chart.setLegendVisible(false);
        chart.setBlockHeight(18);

        // Configuring the axis.
        xAxis.setMinorTickCount(4);
        xAxis.setLabel("Time (weight)");
        yAxis.setTickLabelGap(10);
        yAxis.setLabel("Processor");

        // Registering the processors.
        processorSeries = FXCollections.observableArrayList();
        for (int counter = 0; counter < this.config.getNumOfProcessors(); counter++) {
            processorSeries.add(new XYChart.Series());
        }

        chart.setData(processorSeries);
        updateSchedule(schedule);
    }

    /**
     * This method is used to update the internal representation of the Gantt Chart, using the new
     * version of the schedule.
     * @param schedule a Schedule instance representing the new schedule.
     */
    public void updateSchedule(Schedule schedule) {
        this.schedule = schedule;

        try {

            // Getting the important parts of the schedule.
            byte[] taskMap = this.schedule.getTaskProcessorMap();
            int[] taskStartTime = this.schedule.getTaskStartTimeMap();

            // Clear series before updating.
            for (XYChart.Series se : processorSeries) {
                se.getData().clear();
            }

            for (int counter = 0; counter < tasks.length; counter++) {
                Task currentTask = tasks[counter];
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

    /**
     * This method is used to return a Gantt Chart for the latest schedule in the instance.
     * @return GanttComponent representing the JavaFX Gantt Chart representation.
     */
    public GanttComponent getSchedule() {
        return chart;
    }
}

