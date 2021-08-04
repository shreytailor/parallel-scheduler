package com.team7.visualization.system;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.util.Duration;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

public abstract class UtilizationProvider {
    int time;
    private LineChart<String, Number> chart;
    protected final OperatingSystemMXBean bean;

    public UtilizationProvider(LineChart<String, Number> chart, String title) {
        this.time = 0;
        this.chart = chart;
        chart.setTitle(title);
        chart.setLegendVisible(false);
        bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    public void startTracking() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        chart.setAnimated(false);
        chart.getYAxis().setAutoRanging(false);
        chart.getData().add(series);

        EventHandler<ActionEvent> chartUpdater = event -> {
            ObservableList currentData = series.getData();
            currentData.add(new XYChart.Data<>(String.valueOf(time), getData()));
            if (currentData.size() == 20) currentData.remove(0);
            time++;
        };

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), chartUpdater));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public abstract double getData();
    public abstract double getUpperBound();
}
