package com.team7;

import com.team7.exceptions.CommandLineException;
import com.team7.model.Graph;
import com.team7.model.Schedule;
import com.team7.parsing.Config;
import com.team7.parsing.CLIParser;
import com.team7.parsing.DOTParser;
import com.team7.visualization.VisualizationDriver;
import java.io.FileNotFoundException;

public class Entrypoint {
    public static void main(String[] args) {
        try {
            // Getting the configuration from the command line, and reading the input graph.
            Config config  = CLIParser.parseCommandLineArguments(args);
            Graph graph = DOTParser.read(config.getInputName());

            // Processing the input graph by using the scheduler, and storing the output.
            Scheduler scheduler = new Scheduler(graph, config.getNumOfProcessors());
            Schedule schedule = scheduler.findFeasibleSchedule();
            DOTParser.write(config.getOutputName(),schedule, graph);
            System.out.println("Feasible schedule generated with makespan of " + schedule.getEstimatedFinishTime());

            // Showing the visualization, if requested by the user. Note: not implemented for milestone 1
            //if (config.isVisualised()) {
            //    beginVisualisation(schedule, config);
            //}
        } catch (CommandLineException | FileNotFoundException exception) {
            System.out.println(exception.getMessage());
            System.exit(1);
        }
    }

    private static void beginVisualisation(Schedule schedule, Config config) {
        new Thread(() -> {
            VisualizationDriver.main(schedule, config);
        }).start();
    }
}