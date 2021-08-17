package com.team7.visualization.system;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class TimeProvider {
    private static long time;

    public TimeProvider() {
        time = 0;
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                time += 100;
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public long getCurrentSec() {
        return time/1000;
    }

    public long getCurrentMilli() {
        return time%1000;
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
    }
}
