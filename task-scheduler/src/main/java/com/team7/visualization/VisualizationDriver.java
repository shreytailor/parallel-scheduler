package com.team7.visualization;

import com.team7.model.Schedule;
import com.team7.model.Task;
import com.team7.parsing.Config;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class VisualizationDriver extends Application {
    private static Config _config;
    private static Task[] _tasks;
    private static SchedulerScreenController _controller;

    public static void show(Task[] tasks, Config config) {
        _config = config;
        _tasks = tasks;
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SplashScreen.fxml"));

        _controller = new SchedulerScreenController(_tasks, _config);
        loader.setController(_controller);

        Parent root = loader.load();
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setTitle("The Marauders: Task Visualization");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }

    public static void finish() {
        _controller.stop();
    }
}
