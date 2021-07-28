package com.team7.model;

import org.junit.jupiter.api.Test;
import java.io.FileNotFoundException;

public class DOTParserTest {
    @Test
    public void testDotParser_oneNodeNoEdges() throws FileNotFoundException {
        DOTParser dotParser = new DOTParser();
        dotParser.parse("DOTParser.dot");
    }
}
