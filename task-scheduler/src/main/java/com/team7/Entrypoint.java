package com.team7;


import com.team7.algorithm.ParallelScheduler;
import com.team7.algorithm.Scheduler;
import com.team7.exceptions.CommandLineException;
import com.team7.model.Graph;
import com.team7.model.Schedule;
import com.team7.model.Task;
import com.team7.parsing.Config;
import com.team7.parsing.CLIParser;
import com.team7.parsing.DOTParser;
import com.team7.visualization.VisualizationDriver;
import com.team7.visualization.realtime.ScheduleUpdater;
import javafx.application.Platform;

import java.io.FileNotFoundException;

public class Entrypoint {
    public static void main(String[] args) {
        try {
            // Getting the configuration from the command line, and reading the input graph.
            Config config  = CLIParser.parseCommandLineArguments(args);
            Graph graph = DOTParser.read(config.getInputName());

            // Processing the input graph by using the scheduler, and storing the output.

            Scheduler scheduler = new ParallelScheduler(graph, config.getNumOfProcessors(), config.getNumOfThreads());
            ScheduleUpdater scheduleUpdater = ScheduleUpdater.getInstance();
            scheduleUpdater.setScheduler(scheduler);

            long start = System.currentTimeMillis();
            Schedule schedule = scheduler.findOptimalSchedule();
            long finish = System.currentTimeMillis();

            stopVisualisationTime();
            System.out.println(finish-start);
            DOTParser.write(config.getOutputName(),schedule, graph);

            // Showing the visualization, if requested by the user.
            if (config.isVisualised()) {
                beginVisualisation(scheduler.getTasks() ,schedule, config);
            }
            if (scheduler.getClass() == ParallelScheduler.class) {
                ((ParallelScheduler) scheduler).shutdown();
            }
        } catch (CommandLineException | FileNotFoundException exception) {
            System.out.println(exception.getMessage());
            System.exit(1);
        }
    }

    private static void beginVisualisation(Task[] tasks, Config config) {
        new Thread(() -> {
            VisualizationDriver.show(tasks, config);
        }).start();
    }

    private static void stopVisualisationTime() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                VisualizationDriver.finish();
            }
        });
    }
}