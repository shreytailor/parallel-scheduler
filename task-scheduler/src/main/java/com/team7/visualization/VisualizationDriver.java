package com.team7.visualization;

import com.team7.model.Schedule;
import com.team7.parsing.Config;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class VisualizationDriver extends Application {
    private static Config _config;
    private static Schedule _schedule;

    public static void main(Schedule schedule, Config config) {
        _config = config;
        _schedule = schedule;
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/SchedulerScreen.fxml"));
        Scene scene = new Scene(root);

        primaryStage.setTitle("The Marauders: Task Scheduler Visualization");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
