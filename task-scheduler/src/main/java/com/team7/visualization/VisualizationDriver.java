package com.team7.visualization;

import com.team7.Entrypoint;
import com.team7.algorithm.Scheduler;
import com.team7.model.Schedule;
import com.team7.model.Task;
import com.team7.parsing.Config;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * This class is the driver which initiates the Graphical User Interface, if asked by the user.
 */
public class VisualizationDriver extends Application {
    private static Config config;
    private static Task[] tasks;
    private static Scheduler scheduler;
    private static SchedulerScreenController controller;

    /**
     * This method is used to launch the user interface for the algorithm, after you pass in the
     * required arguments for it to work with.
     * @param tasks the tasks from the .dot file
     * @param config an instance of the Config class which contains data about the CLI parameters.
     * @param scheduler the Scheduler which is doing the scheduling.
     */
    public static void show(Task[] tasks, Config config, Scheduler scheduler) {
        VisualizationDriver.config = config;
        VisualizationDriver.tasks = tasks;
        VisualizationDriver.scheduler = scheduler;
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Load the fxml file, its custom controller, and set it as the main parent.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SplashScreen.fxml"));
        controller = new SchedulerScreenController(tasks, config);
        loader.setController(controller);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        // Basic configuration for the primary stage.
        primaryStage.setScene(scene);
        primaryStage.setTitle("The Marauders: Task Visualization");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();

        // Begin the algorithm on a new thread, so the application is not being blocked.
        new Thread(() -> {
            Schedule schedule = scheduler.findOptimalSchedule();
            Entrypoint.writeScheduleOutputToFile(schedule);
        }).start();
    }

    public static void updateScreen() {
        Platform.runLater(() -> {
            controller.finalUpdate(scheduler);
        });
    }
}
