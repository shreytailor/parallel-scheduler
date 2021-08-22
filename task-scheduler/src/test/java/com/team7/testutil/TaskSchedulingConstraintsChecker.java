package com.team7.testutil;

import com.team7.model.Edge;
import com.team7.model.Graph;
import com.team7.model.Schedule;
import com.team7.model.Task;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class TaskSchedulingConstraintsChecker {

    public static boolean isProcessorConstraintMet(Schedule schedule, Graph graph, int numOfProcessors) {
        // Make a list of tasks that are assigned to each processor
        List<Task>[] processors = getTaskPopulatedProcessors(schedule, graph, numOfProcessors);


        // For each processor, check that each pair of tasks meet processor requirement
        // This check is only done if there's an edge between those pair of tasks
        for (List<Task> processor : processors) {
            // If a processor doesn't have any tasks assigned, break
            if (processor == null) {
                break;
            }
            for (Task taskOne : processor) {
                for (Task taskTwo : processor) {
                    if (taskOne.equals(taskTwo)) {
                        break;
                    }
                    if (!isTaskPairCompatible(taskOne, taskTwo, schedule)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }


    private static boolean isTaskPairCompatible(Task taskOne, Task taskTwo, Schedule schedule) {

        int taskOneStartingTime = schedule.getTaskStartTime(taskOne);
        int taskTwoStartingTime = schedule.getTaskStartTime(taskTwo);

        // Only need to satisfy two conditions as OR
        boolean conditionOne = taskOneStartingTime + taskOne.getWeight() <= taskTwoStartingTime;
        boolean conditionTwo = taskTwoStartingTime + taskTwo.getWeight() <= taskOneStartingTime;

        return conditionOne || conditionTwo;
    }


    private static List<Task>[] getTaskPopulatedProcessors(Schedule schedule, Graph graph, int numOfProcessors) {
        List<Task>[] processors = new ArrayList[numOfProcessors];
        if (schedule == null) {
            fail("Schedule is null. This means that the schedule must have not been generated, or the input graph itself was empty");
        }

        for (Task t : graph.getNodes()) {
            int pid = schedule.getTaskProcessor(t);
            if (processors[pid] == null) {
                processors[pid] = new ArrayList<>();
            }
            processors[pid].add(t);
        }

        return processors;
    }

    public static boolean isPrecedenceConstraintMet(Schedule schedule, List<Edge> edges) {

        // For each edge ij (i being the tail task, j being the head task)
        // If p_i != p_j, then
        // ts_j => ts_i + w_i + c_e (cost of edge)
        // else, ts_j => ts_i + w_i
        for (Edge edge : edges) {
            Task tail = edge.getTail();
            Task head = edge.getHead();
            int tailPid = schedule.getTaskProcessor(tail);
            int headPid = schedule.getTaskProcessor(head);
            int tailStartTime = schedule.getTaskStartTime(tail);
            int headStartTime = schedule.getTaskStartTime(head);

            int commCost = (tailPid == headPid) ? 0 : edge.getWeight();
            boolean isConstraintMet = headStartTime >= tailStartTime + tail.getWeight() + commCost;

            if (!isConstraintMet) {
                return false;
            }
        }

        return true;
    }
}
