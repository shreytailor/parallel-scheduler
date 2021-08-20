package com.team7.visualization.system;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class TimeProvider {
    private static long time;
    private Timeline _clockTimeline;
    private List<Timeline> _timelines;
    private static TimeProvider provider;

    private TimeProvider() {
        time = 0;
        _timelines = new ArrayList<>();

        _clockTimeline = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                time += 100;
            }
        }));
        _clockTimeline.setCycleCount(Timeline.INDEFINITE);
        _clockTimeline.play();
    }

    public static TimeProvider getInstance() {
        if (provider == null) {
            provider = new TimeProvider();
        }
        return provider;
    }

    public long getCurrentSec() {
        return time / 1000;
    }

    public long getCurrentMilli() {
        return time % 1000;
    }

    public void registerLabel(Label timerLabel) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                timerLabel.setText((getCurrentSec()+"."+getCurrentMilli()).replaceAll("0{2,}$", ""));
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        _timelines.add(timeline);
    }

    public void stopTimerLabel() {
        for (Timeline timeline : _timelines) {
            timeline.stop();
        }
    }

    public void registerTimeline(Timeline timeline) {
        _timelines.add(timeline);
    }
}
