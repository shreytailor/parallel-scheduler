package com.team7;

import com.team7.algorithm.Scheduler;
import com.team7.model.Edge;
import com.team7.model.Graph;
import com.team7.model.Schedule;
import com.team7.model.Task;
import com.team7.testutil.TaskSchedulingConstraintsChecker;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SchedulerTestBasic {
    /**
     * DOES NOT ASSERT
     * Allocate a graph of 3 tasks onto 1 processor
     */
    @Test
    void AStar_singleProcessor3TasksNoDependency() {
        Entrypoint.IS_TEST_MODE = true;
//        Given
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
        Entrypoint.IS_TEST_MODE = true;
//        Given
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

}
