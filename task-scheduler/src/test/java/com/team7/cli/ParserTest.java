package com.team7.cli;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @org.junit.jupiter.api.Test
    void parseCommandLineArguments() {
        String cliString = "INPUT.dot P -p N -v -o OUTPUT";
        String[] arguments = cliString.split(" ");

        Parser.parseCommandLineArguments(arguments);

    }
}