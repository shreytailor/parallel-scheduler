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
    private ObservableList<Schedule> _observableList;

    private ScheduleUpdater() {
        _observableList = FXCollections.observableArrayList();
    }

    public void setScheduler(Scheduler scheduler) {
        _scheduler = scheduler;
        _observableList.add(scheduler.getSharedState());
    }

    public Scheduler getScheduler() {
        return _scheduler;
    }

    public void setObservableList(ObservableList<Schedule> observableList) {
        _observableList = observableList;
    }

    public ObservableList<Schedule> getObservableList() {
        return _observableList;
    }

    public static ScheduleUpdater getInstance () {
        if (_scheduleUpdater == null) {
            _scheduleUpdater = new ScheduleUpdater();
        }

        return _scheduleUpdater;
    }

    public void start() {
        EventHandler<ActionEvent> scheduleUpdater = event -> {
            System.out.println("Trying to update...");

            if (_observableList.size() > 0) {
                _observableList.clear();
            }

            _observableList.add(_scheduler.getSharedState());
            System.out.println(_observableList.get(0));
        };

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), scheduleUpdater));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
}
