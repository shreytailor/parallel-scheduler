package com.team7.model;

import static org.junit.jupiter.api.Assertions.*;
import com.team7.parsing.DOTParser;
import org.junit.jupiter.api.Test;
import java.io.FileNotFoundException;
import java.util.*;

public class DOTParserTest {
    private String testDir = System.getProperty("user.dir") + "/src/dot-tests/";

    /**
     * Test where there are two basic nodes, and an edge in between. The output should contain
     * two nodes and one edge.
     */
    @Test
    public void testDotParser_twoNodesOneEdgeInBetween() throws FileNotFoundException {
        // Given
        DOTParser dotParser = new DOTParser();
        dotParser.read(testDir + "test0.dot");

        // When
        List<Edge> actualEdges = dotParser.getEdges();
        Map<String, Task> actualTasks = dotParser.getTasks();
        List<Edge> expectedEdges = new ArrayList<>();
        Map<String, Task> expectedTasks = new HashMap<>();
        Task a = new Task("a", 1);
        Task b = new Task("b", 1);
        Edge aToB = new Edge(b, a, 1);
        expectedEdges.add(aToB);
        expectedTasks.put("a", a);
        expectedTasks.put("b", b);

        // Then
        assertEquals(expectedEdges, actualEdges);
        assertEquals(expectedTasks, actualTasks);
    }

    /**
     * Test where there are two basic nodes, and no edges in between them.
     */
    @Test
    public void testDotParser_twoNodesNoEdges() throws FileNotFoundException {
        // Given
        DOTParser dotParser = new DOTParser();
        dotParser.read(testDir + "test1.dot");

        // When
        List<Edge> actualEdges = dotParser.getEdges();
        Map<String, Task> actualTasks = dotParser.getTasks();
        List<Edge> expectedEdges = new ArrayList<>();
        Map<String, Task> expectedTasks = new HashMap<>();
        Task a = new Task("a", 1);
        Task b = new Task("b", 1);
        expectedTasks.put("a", a);
        expectedTasks.put("b", b);

        // Then
        assertEquals(expectedEdges, actualEdges);
        assertEquals(expectedTasks, actualTasks);
    }

    /**
     * Test where there are three nodes, but edges are such that it forms a circular directed graph.
     */
    @Test
    public void testDotParser_threeNodesCircularDirectedGraph() throws FileNotFoundException {
        // Given
        DOTParser dotParser = new DOTParser();
        dotParser.read(testDir + "test2.dot");

        // When
        List<Edge> actualEdges = dotParser.getEdges();
        Map<String, Task> actualTasks = dotParser.getTasks();
        List<Edge> expectedEdges = new ArrayList<>();
        Map<String, Task> expectedTasks = new HashMap<>();
        Task a = new Task("a", 1);
        Task b = new Task("b", 1);
        Task c = new Task("c", 1);
        Edge aToB = new Edge(b, a, 1);
        Edge bToC = new Edge(c, b, 1);
        Edge cToA = new Edge(a, c, 1);
        expectedTasks.put("a", a);
        expectedTasks.put("b", b);
        expectedTasks.put("c", c);
        expectedEdges.add(aToB);
        expectedEdges.add(bToC);
        expectedEdges.add(cToA);

        // Then
        assertEquals(expectedEdges, actualEdges);
        assertEquals(expectedTasks, actualTasks);
    }

    /**
     * Test where there are three nodes, and the root node is connected to the two other nodes
     * using a weighed edge.
     */
    @Test
    public void testDotParser_threeNodesAllConnectedWithARoot() throws FileNotFoundException {
        // Given
        DOTParser dotParser = new DOTParser();
        dotParser.read(testDir + "test3.dot");

        // When
        List<Edge> actualEdges = dotParser.getEdges();
        Map<String, Task> actualTasks = dotParser.getTasks();
        List<Edge> expectedEdges = new ArrayList<>();
        Map<String, Task> expectedTasks = new HashMap<>();
        Task a = new Task("a", 1);
        Task b = new Task("b", 1);
        Task c = new Task("c", 1);
        Edge aToB = new Edge(b, a, 1);
        Edge aToC = new Edge(c, a, 1);
        expectedTasks.put("a", a);
        expectedTasks.put("b", b);
        expectedTasks.put("c", c);
        expectedEdges.add(aToB);
        expectedEdges.add(aToC);

        // Then
        assertEquals(expectedEdges, actualEdges);
        assertEquals(expectedTasks, actualTasks);
    }

    /**
     * Test where there are three nodes, out of which two are connected with an edge but one isn't.
     */
    @Test
    public void testDotParser_threeNodesWithTwoConnectedNodes() throws FileNotFoundException {
        // Given
        DOTParser dotParser = new DOTParser();
        dotParser.read(testDir + "test4.dot");

        // When
        List<Edge> actualEdges = dotParser.getEdges();
        Map<String, Task> actualTasks = dotParser.getTasks();
        List<Edge> expectedEdges = new ArrayList<>();
        Map<String, Task> expectedTasks = new HashMap<>();
        Task a = new Task("a", 1);
        Task b = new Task("b", 1);
        Task c = new Task("c", 1);
        Edge aToB = new Edge(b, a, 1);
        expectedTasks.put("a", a);
        expectedTasks.put("b", b);
        expectedTasks.put("c", c);
        expectedEdges.add(aToB);

        // Then
        assertEquals(expectedEdges, actualEdges);
        assertEquals(expectedTasks, actualTasks);
    }

    /**
     * Test where the graph is just empty.
     */
    @Test
    public void testDotParser_emptyGraph() throws FileNotFoundException {
        // Given
        DOTParser dotParser = new DOTParser();
        dotParser.read(testDir + "test5.dot");

        // When
        List<Edge> actualEdges = dotParser.getEdges();
        Map<String, Task> actualTasks = dotParser.getTasks();
        List<Edge> expectedEdges = new ArrayList<>();
        Map<String, Task> expectedTasks = new HashMap<>();

        // Then
        assertEquals(expectedEdges, actualEdges);
        assertEquals(expectedTasks, actualTasks);
    }

    /**
     * Test where there is only one node in the graph, with no edges. In the output, there should
     * be no edges created.
     */
    @Test
    public void testDotParser_oneNodeNoEdges() throws FileNotFoundException {
        // Given
        DOTParser dotParser = new DOTParser();
        dotParser.read(testDir + "test6.dot");

        // When
        List<Edge> actualEdges = dotParser.getEdges();
        Map<String, Task> actualTasks = dotParser.getTasks();
        List<Edge> expectedEdges = new ArrayList<>();
        Map<String, Task> expectedTasks = new HashMap<>();
        Task a = new Task("a", 1);
        expectedTasks.put("a", a);

        // Then
        assertEquals(expectedEdges, actualEdges);
        assertEquals(expectedTasks, actualTasks);
    }

    /**
     * Test where there is a single node, but it has an edge to itself.
     */
    @Test
    public void testDotParser_oneNodeWithEdgeToItself() throws FileNotFoundException {
        // Given
        DOTParser dotParser = new DOTParser();
        dotParser.read(testDir + "test7.dot");

        // When
        List<Edge> actualEdges = dotParser.getEdges();
        Map<String, Task> actualTasks = dotParser.getTasks();
        List<Edge> expectedEdges = new ArrayList<>();
        Map<String, Task> expectedTasks = new HashMap<>();
        Task a = new Task("a", 1);
        Edge aToA = new Edge(a, a, 1);
        expectedTasks.put("a", a);
        expectedEdges.add(aToA);

        // Then
        assertEquals(expectedEdges, actualEdges);
        assertEquals(expectedTasks, actualTasks);
    }

    /**
     * Test for a more realistic directed graph which is larger and consists of five nodes.
     */
    @Test
    public void testDotParser_complexFeasibleDirectedGraph() throws FileNotFoundException {
        // Given
        DOTParser dotParser = new DOTParser();
        dotParser.read(testDir + "testComplex.dot");

        // When
        List<Edge> actualEdges = dotParser.getEdges();
        Map<String, Task> actualTasks = dotParser.getTasks();
        List<Edge> expectedEdges = new ArrayList<>();
        Map<String, Task> expectedTasks = new HashMap<>();
        Task a = new Task("a", 1);
        Task b = new Task("b", 1);
        Task c = new Task("c", 2);
        Task d = new Task("d", 5);
        Task e = new Task("e", 10);
        Edge aToB = new Edge(b, a, 3);
        Edge aToC = new Edge(c, a, 5);
        Edge cToD = new Edge(d, c, 2);
        Edge bToD = new Edge(d, b, 3);
        Edge dToE = new Edge(e, d, 10);
        expectedTasks.put("a", a);
        expectedTasks.put("b", b);
        expectedTasks.put("c", c);
        expectedTasks.put("d", d);
        expectedTasks.put("e", e);
        expectedEdges.add(aToB);
        expectedEdges.add(aToC);
        expectedEdges.add(cToD);
        expectedEdges.add(bToD);
        expectedEdges.add(dToE);

        // Then
        assertEquals(expectedTasks, actualTasks);
        assertTrue(expectedEdges.size() == actualEdges.size() &&
                expectedEdges.containsAll(actualEdges) && actualEdges.containsAll(expectedEdges));
    }
}
