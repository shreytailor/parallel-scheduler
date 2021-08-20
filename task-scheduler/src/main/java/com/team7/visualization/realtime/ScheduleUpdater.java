package com.team7.visualization.realtime;

import com.team7.algorithm.Scheduler;
import com.team7.model.Schedule;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public class ScheduleUpdater {
    private static ScheduleUpdater _scheduleUpdater;
    private Scheduler _scheduler;
    private Schedule _observedSchedule;
    private Timeline _scheduleUpdaterTimeline;

    public void setScheduler(Scheduler scheduler) {
        _scheduler = scheduler;
    }

    public Scheduler getScheduler() {
        return _scheduler;
    }

    public Schedule getObservedSchedule() {
        return _observedSchedule;
    }

    public static ScheduleUpdater getInstance () {
        if (_scheduleUpdater == null) {
            _scheduleUpdater = new ScheduleUpdater();
        }

        return _scheduleUpdater;
    }

    public void start() {
        EventHandler<ActionEvent> scheduleUpdater = event -> {
            _observedSchedule = _scheduler.getSharedState();
        };

        _scheduleUpdaterTimeline = new Timeline(new KeyFrame(Duration.millis(500), scheduleUpdater));
        _scheduleUpdaterTimeline.setCycleCount(Timeline.INDEFINITE);
        _scheduleUpdaterTimeline.play();
    }

    public void stop() {
        _observedSchedule = _scheduler.getSharedState();
        _scheduleUpdaterTimeline.stop();
    }
}
