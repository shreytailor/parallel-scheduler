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
    private static Schedule _schedule;
    private static Task[] _tasks;

    public static void show(Task[] tasks, Schedule schedule, Config config) {
        _config = config;
        _schedule = schedule;
        _tasks = tasks;
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SplashScreen.fxml"));
        loader.setController(new SchedulerScreenController(_tasks, _schedule, _config));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setTitle("The Marauders: Task Visualization");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }
}
