package com.team7;

import com.team7.model.Edge;
import com.team7.model.Graph;
import com.team7.model.Schedule;
import com.team7.model.Task;
import com.team7.parsing.DOTParser;
import com.team7.testutil.TaskSchedulingConstraintsChecker;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


class SchedulerTest {

    public static final String DOT_TEST_FILE_DIRECTORY = "src/dot-tests";

    /**
     * DOES NOT ASSERT
     * Allocate a graph of 3 tasks onto 1 processor
     */
    @Test
    void AStar_singleProcessor3TasksNoDependency() {
//        Given
        Task.resetID();
        Task task1 = new Task("a", 3);
        Task task2 = new Task("b", 4);
        Task task3 = new Task("c", 5);
        List<Task> tasks = new ArrayList<>(Arrays.asList(task1, task2, task3));
        Graph g = new Graph(tasks, new ArrayList<>());
        int numProcessors = 1;
        Scheduler scheduler = new Scheduler(g, numProcessors);
//        When
        Schedule result = scheduler.findOptimalSchedule();

//        Then
        assertTrue(TaskSchedulingConstraintsChecker.isProcessorConstraintMet(result, g, numProcessors));
    }


    /**
     * DOES NOT ASSERT
     * This tests AStar algorithm's handling of dependencies, for single processor task allocation
     */
    @Test
    void AStar_singleProcessor3Tasks() {
//        Given
        Task.resetID();
        Task task1 = new Task("a", 3);
        Task task2 = new Task("b", 4);
        Task task3 = new Task("c", 5);

        Edge edge1 = new Edge(task1, task2, 3);
        Edge edge2 = new Edge(task1, task3, 4);
        Edge edge3 = new Edge(task2, task3, 5);

        task1.addIngoingEdge(edge1);
        task2.addOutgoingEdge(edge1);

        task1.addIngoingEdge(edge2);
        task3.addOutgoingEdge(edge2);

        task2.addIngoingEdge(edge3);
        task3.addOutgoingEdge(edge3);

        List<Task> tasks = new ArrayList<>(Arrays.asList(task1, task2, task3));
        List<Edge> edges = new ArrayList<>(Arrays.asList(edge1, edge2, edge3));
        Graph graph = new Graph(tasks, edges);
        int numProcessors = 1;
        Scheduler scheduler = new Scheduler(graph, numProcessors);

//        When
        Schedule result = scheduler.findOptimalSchedule();
        assertTrue(TaskSchedulingConstraintsChecker.isProcessorConstraintMet(result, graph, numProcessors));
    }

    @TestFactory
    Collection<DynamicTest> dynamicTestsWithCollection() {
        List<DynamicTest> tests = new ArrayList<>();

        File directory = new File(DOT_TEST_FILE_DIRECTORY);
        for (File file : directory.listFiles()) {
            tests.add(
                    DynamicTest.dynamicTest(
                            file.getName(),
                            () -> testAStarWithDotFile(file)
                    ));
        }

        return tests;
    }

    private void testAStarWithDotFile(File file) {
        // given
        try {
            Graph g = DOTParser.read(file.toString());
            // when
            int numProcessors = 2;
            Scheduler scheduler = new Scheduler(g, numProcessors);
            Schedule result = scheduler.findOptimalSchedule();

            // then
            if (shouldBeNullSchedule(file)) {
                assertNull(result);
            } else {
                assertTrue(TaskSchedulingConstraintsChecker.isProcessorConstraintMet(result, g, numProcessors));
                assertTrue(TaskSchedulingConstraintsChecker.isPrecedenceConstraintMet(result, g.getEdges()));
            }

            System.out.println("schedule = " + result);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    private boolean shouldBeNullSchedule(File file) {
        String fileName = file.getName();

        return fileName.contains("cycle") || fileName.contains("empty");
    }
}