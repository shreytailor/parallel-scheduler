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
import com.team7.visualization.system.TimeProvider;
import javafx.application.Platform;

import java.io.FileNotFoundException;
import java.sql.SQLOutput;

public class Entrypoint {
    public static boolean IS_TEST_MODE = false;
    public static Config config;
    public static Graph graph;

    public static void main(String[] args) {
        try {
            // Getting the configuration from the command line, and reading the input graph.
            config = CLIParser.parseCommandLineArguments(args);
            graph = DOTParser.read(config.getInputName());

            // Processing the input graph by using the scheduler, and storing the texoutput.
            Scheduler scheduler;
            if (config.getNumOfThreads() > 1) {
                scheduler = new ParallelScheduler(graph, config.getNumOfProcessors(), config.getNumOfThreads());
            } else {
                scheduler = new Scheduler(graph, config.getNumOfProcessors());
            }

            ScheduleUpdater scheduleUpdater = ScheduleUpdater.getInstance();
            scheduleUpdater.setScheduler(scheduler);

            if (config.isVisualised()) {
                beginVisualisation(scheduler.getTasks(), config, scheduler);
            } else {
                long start = System.currentTimeMillis();
                System.out.println("Started");
                Schedule schedule = scheduler.findOptimalSchedule();
                System.out.println("Finished");
                long finish = System.currentTimeMillis();
                System.out.println("Time taken: " + (finish - start) + "ms");
                System.out.println("Optimal schedule generated with makespan of " + schedule.getEstimatedFinishTime());
                Entrypoint.writeScheduleOutputToFile(schedule);
                System.exit(0);
            }
        } catch (CommandLineException | FileNotFoundException exception) {
            System.out.println(exception.getMessage());
            System.exit(1);
        }
    }

    public static void writeScheduleOutputToFile(Schedule schedule) {
        DOTParser.write(config.getOutputName(), schedule, graph);
    }

    public static void stopTimerLabel() {
        if (config != null) {
            if (config.isVisualised()) {
                TimeProvider.getInstance().stopTimers();
                VisualizationDriver.updateScreen();
            }
        } else {
            if (!IS_TEST_MODE) {
                throw new RuntimeException("Config should have been initialised");
            }
        }
    }

    private static void beginVisualisation(Task[] tasks, Config config, Scheduler scheduler) {
        new Thread(() -> {
            VisualizationDriver.show(tasks, config, scheduler);
        }).start();
    }

}