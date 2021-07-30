package com.team7;

import com.team7.exceptions.CommandLineException;
import com.team7.model.Edge;
import com.team7.model.Graph;
import com.team7.model.Schedule;
import com.team7.model.Task;
import com.team7.parsing.Config;
import com.team7.parsing.CLIParser;
import com.team7.parsing.DOTParser;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Entrypoint {
    public static void main(String[] args) {
        try {
            Config config  = CLIParser.parseCommandLineArguments(args);
            System.out.println(config);
            DOTParser dotParser = new DOTParser();
            Graph g = dotParser.read(config.getInputName());

            Scheduler scheduler = new Scheduler();

            Schedule schedule = scheduler.AStar(g.getNodes(), config.getNumOfProcessors());
            dotParser.write(config.getOutputName(),schedule, g.getEdges());
        } catch (CommandLineException | FileNotFoundException exception) {
            System.out.println(exception.getMessage());
            System.exit(1);
        }
    }
}