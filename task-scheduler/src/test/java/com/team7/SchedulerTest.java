package com.team7;

import com.team7.model.Edge;
import com.team7.model.Schedule;
import com.team7.model.Task;
import com.team7.parsing.DOTParser;
import com.team7.testutil.TaskSchedulingConstraintsChecker;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


class SchedulerTest {

    public static final String DOT_TEST_FILE_DIRECTORY = "src/20testdotfiles";

    /**
     * DOES NOT ASSERT
     * Allocate a graph of 3 tasks onto 1 processor
     *
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
        assertTrue(TaskSchedulingConstraintsChecker.isProcessorConstraintMet(result, numProcessors));
    }


    /**
     * DOES NOT ASSERT
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
        assertTrue(TaskSchedulingConstraintsChecker.isProcessorConstraintMet(result, numProcessors));
    }

    @TestFactory
    Collection<DynamicTest> dynamicTestsWithCollection() {
        List<DynamicTest> tests = new ArrayList<>();

        File directory = new File(DOT_TEST_FILE_DIRECTORY);
        for (File file : directory.listFiles()) {
            tests.add(
                    DynamicTest.dynamicTest(
                            file.getName(),
                            ()->testAStarWithDotFile(file)
                    ));
        }

        return tests;
    }

    private void testAStarWithDotFile(File file){
        // given
        DOTParser parser = getDotParser(file);
        Map<String, Task> taskMap = parser.getTasks();

        List<Task> tasks = new ArrayList<>(taskMap.values());
        List<Edge> edges = parser.getEdges();
        Scheduler scheduler = new Scheduler();

        // when
        int numProcessors = 2;
        Schedule result = scheduler.AStar(tasks, numProcessors);

        // then

        if(shouldBeNullSchedule(file)){
            assertNull(result);
        }else{
            assertTrue(TaskSchedulingConstraintsChecker.isProcessorConstraintMet(result, numProcessors));
            assertTrue(TaskSchedulingConstraintsChecker.isPrecedenceConstraintMet(result, numProcessors, edges));
        }

        System.out.println("schedule = " + result);

    }

    private boolean shouldBeNullSchedule(File file) {
        String fileName = file.getName();

        if(fileName.contains("cycle") || fileName.contains("empty")){
            return true;
        }
        return false;
    }

    private DOTParser getDotParser(File file) {
        DOTParser parser = new DOTParser();
        try{
            parser.parse(file.toString());
        }catch(Exception e){
            e.printStackTrace();
            fail();
        }
        return parser;
    }








}