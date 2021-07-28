package com.team7.parsing;

import org.apache.commons.cli.*;

public class CLIParser {

    /**
     *
     * @param args requires commandline arguments to be passed in
     * @return Config object which embeds all the command line configuration.
     */
    public static Config parseCommandLineArguments(String[] args) {
        Config config = new Config();

        config.setInputName(args[0]);
        config.setNumOfProcessors(Integer.parseInt(args[1]));

        // Informing the CLI framework about possible optional parameters which users can enter.
        Options options = new Options();
        options.addOption("p", "processors", true, "cores for execution in parallel (default is sequential).");
        options.addOption("v", "visualisation", false, "visualisation for the algorithm.");
        options.addOption("o", "output", true, "name for the output file.");

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

            // If there are any exceptions, show the documentation to the user.
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("Command Line Parameters", options);
        }

        return config;
    }
}
