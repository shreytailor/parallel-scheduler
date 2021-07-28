package com.team7.parsing;

import com.team7.exceptions.CommandLineException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    /**
     * Test common case for Parser#parseCommandLineArguments method
     * where numOfCores, inputName, isVisualised, outputName are provided
     */
    @Test
    void parseCommandLineArguments_commonCase() {
//        Given
        String cliString = "sampleInput.dot 2 -p 3 -v -o outputName";

//        When
        String[] arguments = cliString.split(" ");
        Config config = CLIParser.parseCommandLineArguments(arguments);
        Config expectedConfig = new Config(
                2,
                3,
                true,
                "outputName.dot",
                "sampleInput.dot");

//      Then
        assertEquals(expectedConfig, config);
    }

    /**
     * Test for Parser#parseCommandLineArguments method
     * where outputName is not given
     * The output name should be <inputName>-output.dot by default
     */
    @Test
    void parseCommandLineArguments_noOutputNameProvided() {
//        Given
        String cliString = "sampleInput.dot 4 -p 3 -v";

//        When
        String[] arguments = cliString.split(" ");
        Config config = CLIParser.parseCommandLineArguments(arguments);
        Config expectedConfig = new Config(
                4,
                3,
                true,
                "sampleInput-output.dot",
                "sampleInput.dot");

//        Then
        assertEquals(expectedConfig, config);
    }

    /**
     * Test for Parser#parseCommandLineArguments method
     * where -v flag is not set
     */
    @Test
    void parseCommandLineArguments_doNotVisualise() {
//        Given
        String cliString = "sampleInput.dot 2 -p 3 -o outputName";

//        When
        String[] arguments = cliString.split(" ");
        Config config = CLIParser.parseCommandLineArguments(arguments);
        Config expectedConfig = new Config(
                2,
                3,
                false,
                "outputName.dot",
                "sampleInput.dot");

//        Then
        assertEquals(expectedConfig, config);
    }

    /**
     * Test for Parser#parseCommandLineArguments method
     * where none of the optional arguments are specified
     * ie., no -v, no -p, no -o
     *
     */
    @Test
    void parseCommandLineArguments_noOptionalArgumentSpecified() {
//        Given
        String cliString = "sampleInput.dot 2";

//        When
        String[] arguments = cliString.split(" ");
        Config config = CLIParser.parseCommandLineArguments(arguments);
        Config expectedConfig = new Config(
                2,
                1,
                false,
                "sampleInput-output.dot",
                "sampleInput.dot");

//        Then
        assertEquals(expectedConfig, config);
    }


    /**
     * Test for Parser#parseCommandLineArguments method
     * where a weird flag is put in
     *
     */
    @Test
    void parseCommandLineArguments_unrecognisedFlagThrowsException() {
//        Given
        String cliString = "sampleInput.dot 3 -w 123";

//        When
        String[] arguments = cliString.split(" ");

//        Then
//        TODO: change Exception to appropriate assertion, with appropriate message
        try {
            Config config = CLIParser.parseCommandLineArguments(arguments);
            fail();
        } catch (Exception e) {
            assertEquals("Flag unrecognised", e.getMessage());
        }
    }

    /**
     * Test for Parser#parseCommandLineArguments method
     * where a weird flag is put in
     *
     */
    @Test
    void parseCommandLineArguments_nothingSpecified() {
//        Given
        String cliString = "";

//        When
        String[] arguments = cliString.split("");

//        Then
//        TODO: change Exception to appropriate assertion, with appropriate message
        try {
            Config config = CLIParser.parseCommandLineArguments(arguments);
            fail();
        } catch (Exception e) {
            assertEquals("Not enough parameters specified", e.getMessage());
        }
    }
}