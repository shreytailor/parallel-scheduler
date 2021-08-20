package com.team7.visualization.realtime;

import com.team7.algorithm.Scheduler;
import com.team7.model.Schedule;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;
import org.omg.DynamicAny._DynFixedStub;

public class ScheduleUpdater {
    private static ScheduleUpdater _scheduleUpdater;
    private Scheduler _scheduler;
    private Schedule _observedSchedule;
    private int _openedStates;
    private int _closedStates;
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
            _openedStates = _scheduler.getInfoOpenStates();
            _closedStates = _scheduler.getInfoClosedStates();
        };

        _scheduleUpdaterTimeline = new Timeline(new KeyFrame(Duration.millis(500), scheduleUpdater));
        _scheduleUpdaterTimeline.setCycleCount(Timeline.INDEFINITE);
        _scheduleUpdaterTimeline.play();
    }

    public void stop() {
        _observedSchedule = _scheduler.getSharedState();
        _scheduleUpdaterTimeline.stop();
    }

    public int getOpenedStates() {
        System.out.println("Opened:" + _openedStates);
        return _openedStates;
    }

    public int getClosedStates() {
        System.out.println("Closed:" + _closedStates);
        return _closedStates;
    }

    public void setOpenedStates(int openedStates) {
        _openedStates = openedStates;
    }

    public void setClosedStates(int closedStates) {
        _closedStates = closedStates;
    }
}
