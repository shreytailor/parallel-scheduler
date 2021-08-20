package com.team7.visualization.system;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * This class helps to manage the clock which is maintained within the application (especially to
 * synchronize the time between the visualization and the scheduler (algorithm). It also helps to
 * stop the clock when the algorithm is completed, as well as stopping the JavaFX timelines from
 * the visualization which are used to update the scheduler viewer and the different statistics.
 */
public class TimeProvider {
    private static long time;
    private Timeline _clockTimeline;
    private List<Timeline> _timelines;
    private static TimeProvider provider;

    /**
     * This lone constructor is called by the getInstance() method because this is a Singleton.
     */
    private TimeProvider() {

        // Set the initial time to zero, and initialize the list to store the JavaFX Timelines.
        time = 0;
        _timelines = new ArrayList<>();

        /*
        This is the timeline which tracks the main clock of the application - updating every 100ms.
         */
        _clockTimeline = new Timeline(new KeyFrame(Duration.millis(100), event -> time += 100));
        _clockTimeline.setCycleCount(Timeline.INDEFINITE);
        _clockTimeline.play();
    }

    /**
     * This class is a Singleton, so this method is used to get the singleton instance.
     * @return TimeProvider the singleton instance of this class.
     */
    public static TimeProvider getInstance() {
        if (provider == null) {
            provider = new TimeProvider();
        }
        return provider;
    }

    /**
     * This method is used to pass a JavaFX Label into this class, on which we want to show the time
     * that has elapsed since the algorithm has begun.
     * @param timerLabel the label on which we want to display the current time.
     */
    public void registerLabel(Label timerLabel) {

        // Create a new timeline which updates the label passed, every 100ms.
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                timerLabel.setText((getCurrentSec()+"."+getCurrentMilli()).replaceAll("0{2,}$", ""));
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Add the created timeline to the list of timelines in the class.
        _timelines.add(timeline);
    }

    /**
     * This method is used to stop the timer of the application. It pretty much stops the timelines
     * throughout the visualization, so that everything comes to a halt.
     */
    public void stopTimerLabel() {

        // Go through all the timelines in the list, and stop them.
        for (Timeline timeline : _timelines) {
            timeline.stop();
        }
    }

    /**
     * This method is used to pass a JavaFX timeline to this class, in order to track.
     * @param timeline
     */
    public void registerTimeline(Timeline timeline) {
        _timelines.add(timeline);
    }

    /**
     * Getter for the current number of seconds passed.
     * @return Integer for the number of seconds.
     */
    public long getCurrentSec() {
        return time / 1000;
    }

    /**
     * Getter for the current number of milliseconds in the second.
     * @return Integer for the number of milliseconds in the second.
     */
    public long getCurrentMilli() {
        return time % 1000;
    }
}
