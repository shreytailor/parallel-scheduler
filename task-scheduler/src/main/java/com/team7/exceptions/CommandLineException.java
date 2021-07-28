package com.team7.exceptions;

/**
 * This class is a custom Exception, which is used when there are errors parsing the Command
 * Line arguments from the users.
 */
public class CommandLineException extends Exception {

    /**
     * The default constructor for this exception.
     * @param exceptionMessage clear description of the exception.
     */
    public CommandLineException(String exceptionMessage) {
        super(exceptionMessage);
    }
}
