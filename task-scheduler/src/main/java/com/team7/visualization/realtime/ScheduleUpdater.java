package com.team7.visualization.realtime;

import com.team7.algorithm.Scheduler;
import com.team7.model.Schedule;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

/**
 * This class is a middleware between the Scheduler (actual algorithm), and the Graphical User
 * Interface. It is used to get the data from the algorithm and supply it to the visualization as
 * the execution occurs.
 */
public class ScheduleUpdater {
    private int openedStates;
    private int closedStates;
    private Scheduler scheduler;
    private Schedule observedSchedule;
    private Timeline scheduleUpdaterTimeline;
    private static ScheduleUpdater scheduleUpdater;

    /**
     * This lone constructor is called by the getInstance() method to create the Singleton instance.
     */
    private ScheduleUpdater() {

    }

    /**
     * This class is a Singleton, so this method is used to get the singleton instance.
     * @return ScheduleUpdater the singleton instance of this class.
     */
    public static ScheduleUpdater getInstance () {
        if (scheduleUpdater == null) {
            scheduleUpdater = new ScheduleUpdater();
        }

        return scheduleUpdater;
    }

    /**
     * This method starts to retrieve the information from the scheduler.
     */
    public void start() {
        EventHandler<ActionEvent> scheduleUpdater = event -> {

            // Fetch the latest statistics / information from the scheduler.
            observedSchedule = scheduler.getSharedState();
            openedStates = scheduler.getInfoOpenStates();
            closedStates = scheduler.getInfoClosedStates();
        };

        scheduleUpdaterTimeline = new Timeline(new KeyFrame(Duration.millis(500), scheduleUpdater));
        scheduleUpdaterTimeline.setCycleCount(Timeline.INDEFINITE);
        scheduleUpdaterTimeline.play();
    }

    /**
     * This method starts the process of retrieving the information from the scheduler.
     */
    public void stop() {
        observedSchedule = scheduler.getSharedState();
        scheduleUpdaterTimeline.stop();
    }

    /**
     * This method is used to set the scheduler, from which this class retrieves its information.
     * @param scheduler the scheduler on which the algorithm is running.
     */
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Getter method for the amount of opened states.
     * @return Integer for the number of opened states.
     */
    public int getOpenedStates() {
        return openedStates;
    }

    /**
     * Getter method for the amount of closed states.
     * @return Integer for the number of closed states.
     */
    public int getClosedStates() {
        return closedStates;
    }

    /**
     * This method is used to get the current latest schedule from the scheduler.
     * @return Schedule this is the latest schedule from the scheduler.
     */
    public Schedule getObservedSchedule() {
        return observedSchedule;
    }
}
