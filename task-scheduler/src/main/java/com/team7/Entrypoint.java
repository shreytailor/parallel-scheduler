package com.team7;

import com.team7.exceptions.CommandLineException;
import com.team7.model.Edge;
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
            DOTParser dotParser = new DOTParser();
            dotParser.parse(config.getInputName());

            Scheduler scheduler = new Scheduler();
            List<Task> tasks = new ArrayList<>();
            Iterator taskIterator = dotParser.getTasks().entrySet().iterator();
            while (taskIterator.hasNext()) {
                Task task = (Task) ((Map.Entry )taskIterator.next()).getValue();
                tasks.add(task);
            }

            Schedule schedule = scheduler.AStar(tasks, config.getNumOfProcessors());
            System.out.println(schedule.toString());
        } catch (CommandLineException | FileNotFoundException exception) {
            System.out.println(exception.getMessage());
            System.exit(1);
        }
    }
}