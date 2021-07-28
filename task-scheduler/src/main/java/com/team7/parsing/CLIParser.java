package com.team7.parsing;

import com.team7.exceptions.CommandLineException;
import org.apache.commons.cli.*;

public class CLIParser {

    /**
     * This static method is used to construct a Config object from the parameters provided by
     * the user through the command line.
     *
     * @param args the command line arguments to be passed in.
     * @return Config object which embeds all the command line configuration.
     */
    public static Config parseCommandLineArguments(String[] args) throws CommandLineException {
        Config config = new Config();

        // Informing the CLI framework about possible optional parameters which users can enter.
        Options options = new Options();
        options.addOption("p", "processors", true, "cores for execution in parallel (default is sequential).");
        options.addOption("v", "visualisation", false, "visualisation for the algorithm.");
        options.addOption("o", "output", true, "name for the output file.");

        if (args.length <= 1) {
            printHelp(options);
            throw new CommandLineException("Not enough parameters specified");
        }

        config.setInputName(args[0]);
        config.setNumOfProcessors(Integer.parseInt(args[1]));

        CommandLine commandLine = null;
        CommandLineParser parser = new DefaultParser();

        try {
            commandLine = parser.parse(options, args);
            config.setVisualised(commandLine.hasOption("visualisation"));

            // Error handling for the processors, in the case where user doesn't specify a number.
            String numOfCores = commandLine.getOptionValue("processors");
            if (numOfCores != null) {
                config.setNumOfCores(Integer.parseInt(numOfCores));
            }

            String outputName = commandLine.getOptionValue("output");
            if (outputName == null) {

                // Using the input name and appending '-output', if output path isn't specified.
                String[] fileSegments = config.getInputName().split("\\.(?=[^\\.]+$)");
                outputName = fileSegments[0] + "-output." + fileSegments[1];
                config.setOutputName(outputName);
            } else {

                // Using the output name, as specified by the user.
                config.setOutputName(outputName + ".dot");
            }

        } catch (ParseException exception) {
            printHelp(options);
            throw new CommandLineException("Flag unrecognised");
        }

        return config;
    }

    /**
     * This private method is used to print the command line help.
     * @param options the options for the command line.
     */
    private static void printHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("Command Line Parameters", options);
    }
}
