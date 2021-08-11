package com.team7.algorithm;

import com.team7.model.Edge;
import com.team7.model.Task;

import java.util.LinkedList;
import java.util.Queue;

public class Preprocessor {

    /**
     * Static Level: Static level of a node is its bottom level without counting edge costs.
     * Bottom Level: Bottom level of a node is the length of the longest path from node to an exit node.
     * @param taskBottomLevelMap
     * @param taskStaticLevelMap
     * @param tasks
     */
    public static void calculateTaskStaticAndBottomLevels(int[] taskBottomLevelMap, int[] taskStaticLevelMap, Task[] tasks) {
        for (int i = 0; i < tasks.length; i++) {
            Task task = tasks[i];
            if (task.getOutgoingEdges().size() == 0) {
                taskBottomLevelMap[i] = task.getWeight();
                taskStaticLevelMap[i] = task.getWeight();
                Queue<Task> taskQueue = new LinkedList<>();
                taskQueue.add(task);
                while (taskQueue.size() > 0) {
                    Task t = taskQueue.poll();
                    for (Edge e : t.getIngoingEdges()) {
                        int neighbour = e.getTail().getUniqueID();
                        taskBottomLevelMap[neighbour] = Math.max(taskBottomLevelMap[neighbour], taskBottomLevelMap[t.getUniqueID()] + tasks[neighbour].getWeight() + e.getWeight());
                        taskStaticLevelMap[neighbour] = Math.max(taskStaticLevelMap[neighbour], taskStaticLevelMap[t.getUniqueID()] + tasks[neighbour].getWeight());
                        taskQueue.add(tasks[neighbour]);
                    }
                }
            }
        }
    }

    /**
     * Top level of a node is the length of the longest path from an entry node to the node
     * @param taskTopLevelMap
     * @param tasks
     */
    public static void calculateTaskTopLevels(int[] taskTopLevelMap, Task[] tasks) {
        for (int i = 0; i < tasks.length; i++) {
            Task task = tasks[i];
            if (task.getIngoingEdges().size() == 0) {
                taskTopLevelMap[i] = 0;
                Queue<Task> taskQueue = new LinkedList<>();
                taskQueue.add(task);
                while (taskQueue.size() > 0) {
                    Task t = taskQueue.poll();
                    for (Edge e : t.getOutgoingEdges()) {
                        int neighbour = e.getHead().getUniqueID();
                        taskTopLevelMap[neighbour] = Math.max(taskTopLevelMap[neighbour], taskTopLevelMap[t.getUniqueID()] + t.getWeight() + e.getWeight());
                        taskQueue.add(tasks[neighbour]);
                    }
                }
            }
        }
    }

    /**
     * Record each task as having prerequisites (requirements) or beginnable (no requirements).
     * @param tasks
     * @param taskRequirementsMap
     * @param beginnableTasks
     */
    public static void calculateRequirements(Task[] tasks, byte[] taskRequirementsMap, Queue<Task> beginnableTasks) {
        for (Task task : tasks) {
            if (task.getIngoingEdges().size() != 0) {
                taskRequirementsMap[task.getUniqueID()] = (byte) task.getIngoingEdges().size();
            } else {
                beginnableTasks.add(task);
            }
        }
    }
}
