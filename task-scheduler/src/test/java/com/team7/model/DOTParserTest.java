package com.team7.model;

import static org.junit.jupiter.api.Assertions.*;
import com.team7.parsing.DOTParser;
import org.junit.jupiter.api.Test;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DOTParserTest {
    private String testDir = System.getProperty("user.dir") + "/src/dot-tests/";

    /**
     * Test where there is only one node in the graph, with no edges. In the output, there should
     * be no edges created.
     */
    @Test
    public void testDotParser_oneNodeNoEdges() throws FileNotFoundException {
        // Given
        DOTParser dotParser = new DOTParser();
        dotParser.parse(testDir + "digraphOne.dot");

        // When
        List<Edge> actualEdges = dotParser.getEdges();
        Map<String, Task> actualTasks = dotParser.getTasks();
        Task task = new Task("a", 2);
        List<Edge> expectedEdges = new ArrayList<>();
        Map<String, Task> expectedTasks = new HashMap<>();
        expectedTasks.put("a", task);

        // Then
        assertTrue(actualEdges.equals(expectedEdges) && actualTasks.equals(expectedTasks));
    }
}
