package com.team7;

import com.team7.model.Edge;
import com.team7.model.Schedule;
import com.team7.model.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {

    /**
     * Allocate a graph of 3 tasks onto 1 processor
     */
    @Test
    void AStar_singleProcessor3TasksNoDependency() {
//        Given
        Scheduler scheduler = new Scheduler();
        int numProcessors = 1;
        List<Task> tasks = new ArrayList<>();

        Task task1 = new Task("a",3);
        Task task2 = new Task("b", 4);
        Task task3 = new Task("c", 5);
        tasks.addAll(Arrays.asList(task1, task2, task3));


//        When
        Schedule result = scheduler.AStar(tasks, numProcessors);

//        Then
//        TODO: precedence/dependence constraint tests
        System.out.println("result = " + result);
    }


    /**
     * This tests AStar algorithm's handling of dependencies, for single processor task allocation
     */
    @Test
    void AStar_singleProcessor3Tasks() {
//        Given
        Scheduler scheduler = new Scheduler();
        int numProcessors = 1;
        List<Task> tasks = new ArrayList<>();

        Task task1 = new Task("a",3);
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

        tasks.addAll(Arrays.asList(task1, task2, task3));



//        When
        Schedule result = scheduler.AStar(tasks, numProcessors);

//        Then
//        TODO: precedence/dependence constraint tests
        System.out.println("result = " + result);
    }





}