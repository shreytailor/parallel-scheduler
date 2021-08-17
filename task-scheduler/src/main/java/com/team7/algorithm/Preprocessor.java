package com.team7.algorithm;

import com.team7.model.Edge;
import com.team7.model.Task;

import java.util.*;

public class Preprocessor {
    /**
     * Static Level: Static level of a node is its bottom level without counting edge costs.
     * Bottom Level: Bottom level of a node is the length of the longest path from node to an exit node.
     *
     * @param tasks
     */
    public static int[] calculateTaskStaticLevels(Task[] tasks) {
        int[] taskStaticLevelMap = new int[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            Task task = tasks[i];
            if (task.getOutgoingEdges().size() == 0) {
                taskStaticLevelMap[i] = task.getWeight();
                Queue<Task> taskQueue = new LinkedList<>();
                taskQueue.add(task);
                while (taskQueue.size() > 0) {
                    Task t = taskQueue.poll();
                    for (Edge e : t.getIngoingEdges()) {
                        int neighbour = e.getTail().getUniqueID();
                        taskStaticLevelMap[neighbour] = Math.max(taskStaticLevelMap[neighbour], taskStaticLevelMap[t.getUniqueID()] + tasks[neighbour].getWeight());
                        taskQueue.add(tasks[neighbour]);
                    }
                }
            }
        }
        return taskStaticLevelMap;
    }

    public static int[] calculateTaskBottomLevels(Task[] tasks) {
        int[] taskBottomLevelMap = new int[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            Task task = tasks[i];
            if (task.getOutgoingEdges().size() == 0) {
                taskBottomLevelMap[i] = task.getWeight();
                Queue<Task> taskQueue = new LinkedList<>();
                taskQueue.add(task);
                while (taskQueue.size() > 0) {
                    Task t = taskQueue.poll();
                    for (Edge e : t.getIngoingEdges()) {
                        int neighbour = e.getTail().getUniqueID();
                        taskBottomLevelMap[neighbour] = Math.max(taskBottomLevelMap[neighbour], taskBottomLevelMap[t.getUniqueID()] + tasks[neighbour].getWeight() + e.getWeight());
                        taskQueue.add(tasks[neighbour]);
                    }
                }
            }
        }
        return taskBottomLevelMap;
    }

    /**
     * Top level of a node is the length of the longest path from an entry node to the node
     *
     * @param tasks
     */
    public static int[] calculateTaskTopLevels(Task[] tasks) {
        int[] taskTopLevelMap = new int[tasks.length];
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
        return taskTopLevelMap;
    }

    /**
     * Record each task as having prerequisites (requirements) or beginnable (no requirements).
     *
     * @param tasks
     */
    public static byte[] calculateRequirements(Task[] tasks) {
        byte[] taskRequirementsMap = new byte[tasks.length];
        for (Task task : tasks) {
            taskRequirementsMap[task.getUniqueID()] = (byte) task.getIngoingEdges().size();
        }
        return taskRequirementsMap;
    }

    public static Task[] getTopologicalOrder(List<Task> tasks) {
        Task[] result = new Task[tasks.size()];
        Map<Task,Integer> taskRequirementsMap = new HashMap<>();
        Queue<Task> beginnable = new LinkedList<>();
        for (Task t : tasks) {
            taskRequirementsMap.put(t,t.getIngoingEdges().size());
            if (t.getIngoingEdges().size() == 0) {
                beginnable.add(t);
            }
        }
        int index=0;
        while (beginnable.size() > 0) {
            Task t = beginnable.poll();
            result[index] = t;
            index++;
            for (Edge e : t.getOutgoingEdges()) {
                taskRequirementsMap.compute(e.getHead(), (k,v)-> {
                    v--;
                    if (v==0) {
                        beginnable.add(k);
                    }
                    return v;
                });
            }
        }
        return result;
    }

    //Currently not in use
    public static List[] calculateEquivalentTasks(Task[] tasks) {
        List<Task>[] taskEquivalences = new List[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            Task task1 = tasks[i];
            if (taskEquivalences[task1.getUniqueID()] != null) {
                continue;
            }
            List<Task> equivalent = new LinkedList<>();
            equivalent.add(task1);
            taskEquivalences[task1.getUniqueID()] = equivalent;

            for (int j = i + 1; j < tasks.length; j++) {
                Task task2 = tasks[j];
                if (taskEquivalences[task2.getUniqueID()] != null) {
                    continue;
                }
                boolean equal = true;
                if (task1.getWeight() == task2.getWeight() &&
                        task1.getOutgoingEdges().size() == task2.getOutgoingEdges().size() &&
                        task1.getIngoingEdges().size() == task2.getIngoingEdges().size()) {
                    List<Edge> task1Out = task1.getOutgoingEdges();
                    List<Edge> task2Out = task2.getOutgoingEdges();
                    task1Out.sort(Comparator.comparingInt(a -> a.getHead().getUniqueID()));
                    task2Out.sort(Comparator.comparingInt(a -> a.getHead().getUniqueID()));
                    for (int k = 0; k < task1Out.size(); k++) {
                        if (task1Out.get(k).getHead().getUniqueID() != task2Out.get(k).getHead().getUniqueID()) {
                            equal = false;
                            break;
                        }
                    }

                    List<Edge> task1In = task1.getIngoingEdges();
                    List<Edge> task2In = task2.getIngoingEdges();
                    task1In.sort(Comparator.comparingInt(a -> a.getTail().getUniqueID()));
                    task2In.sort(Comparator.comparingInt(a -> a.getTail().getUniqueID()));
                    for (int k = 0; k < task1In.size(); k++) {
                        if (task1In.get(k).getTail().getUniqueID() != task2In.get(k).getTail().getUniqueID()) {
                            equal = false;
                            break;
                        }
                    }
                } else {
                    equal = false;
                }
                if (equal) {
                    System.out.println(task1.getName() + " is equivalent to " + task2.getName());
                    equivalent.add(task2);
                    taskEquivalences[task2.getUniqueID()] = equivalent;
                }
            }
        }
        return taskEquivalences;
    }
}
