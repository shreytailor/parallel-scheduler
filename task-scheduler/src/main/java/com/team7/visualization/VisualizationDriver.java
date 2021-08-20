package com.team7.visualization;

import com.team7.algorithm.Scheduler;
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
    private static Scheduler scheduler;

    public static void show(Task[] tasks, Config config, Scheduler s) {
        _config = config;
        _tasks = tasks;
        scheduler = s;
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SplashScreen.fxml"));

        _controller = new SchedulerScreenController(_tasks, _config, scheduler);
        loader.setController(_controller);

        Parent root = loader.load();
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setTitle("The Marauders: Task Visualization");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();

        new Thread(() -> {
            Schedule schedule = scheduler.findOptimalSchedule();
        }).start();


    }

    public static void finish() {
        _controller.stop();
    }
}
