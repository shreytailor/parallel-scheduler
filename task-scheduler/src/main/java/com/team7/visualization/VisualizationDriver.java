package com.team7.visualization;

import com.team7.model.Schedule;
import javafx.application.Application;
import javafx.stage.Stage;

public class VisualizationDriver extends Application {
    private static Schedule _schedule;

    public static void main(Schedule schedule) {
        _schedule = schedule;
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("The Marauders: Task Visualization");
        primaryStage.show();
    }
}
