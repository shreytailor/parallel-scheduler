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

/**
 * This is an abstract UtilizationProvider class which will take a LineChart<String, Number> as an
 * argument in order to start providing tracking data to the graph. You can use implementations
 * of this class, depending on what sort of data you want to display on the graph.
 */
public abstract class UtilizationProvider {

    private LineChart<String, Number> chart;
    protected final OperatingSystemMXBean bean;
    private TimeProvider timeProvider;

    public UtilizationProvider(LineChart<String, Number> chart, String title, TimeProvider tp) {
        this.chart = chart;
        chart.setTitle(title);
        chart.setLegendVisible(false);
        bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        timeProvider = tp;
    }

    /**
     * This method starts displaying the tracked data onto the graph entered in the constructor.
     */
    public void startTracking() {
        // Configure the graph settings.
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        chart.setAnimated(false);
        chart.getYAxis().setAutoRanging(false);
        chart.getData().add(series);

        // Create an event handler which fetches the data from the custom getData() method.
        EventHandler<ActionEvent> chartUpdater = event -> {
            ObservableList currentData = series.getData();
            currentData.add(new XYChart.Data<>(String.valueOf(timeProvider.getCurrentSec()), getData()));
            if (currentData.size() == 20) currentData.remove(0);
        };

        // Finally, register the event listener.
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), chartUpdater));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    public abstract double getData();
    public abstract double getUpperBound();
}
