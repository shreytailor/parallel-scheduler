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
            DOTParser dotParser = new DOTParser();
            Config config  = CLIParser.parseCommandLineArguments(args);
            Graph graph = dotParser.read(config.getInputName());

            // Processing the input graph by using the scheduler, and storing the output.
            Scheduler scheduler = new Scheduler();
            Schedule schedule = scheduler.findOptimalSchedule(graph.getNodes(), config.getNumOfProcessors());
            dotParser.write(config.getOutputName(),schedule, graph.getEdges());

            // Showing the visualization of the output schedule.
            VisualizationDriver.main(schedule, graph.getNodes(), config);
        } catch (CommandLineException | FileNotFoundException exception) {
            System.out.println(exception.getMessage());
            System.exit(1);
        }
    }
}