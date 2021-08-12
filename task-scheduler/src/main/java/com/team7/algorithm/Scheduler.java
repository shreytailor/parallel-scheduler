package com.team7.algorithm;

import com.team7.model.Edge;
import com.team7.model.Graph;
import com.team7.model.Schedule;
import com.team7.model.Task;

import java.util.*;

public class Scheduler {
    private int processors;
    private Task[] tasks;
    private int[] taskTopLevelMap;
    private int[] taskBottomLevelMap;
    private int[] taskStaticLevelMap;
    private byte[] taskRequirementsMap;
    private Schedule feasibleSchedule;
    private int totalComputationTime = 0;
    private Queue<Schedule> scheduleQueue;
    private Set<Schedule> visitedSchedules;

    public Scheduler(Graph g, int numOfProcessors) {
        processors = numOfProcessors;
        int numTasks = g.getNodes().size();
        tasks = new Task[numTasks];
        for (Task t : g.getNodes()) {
            tasks[t.getUniqueID()] = t;
            totalComputationTime += t.getWeight();
        }
        taskTopLevelMap = new int[numTasks];
        taskBottomLevelMap = new int[numTasks];
        taskStaticLevelMap = new int[numTasks];
        taskRequirementsMap = new byte[numTasks];
        scheduleQueue = new PriorityQueue<>((a, b) -> {
            int n = a.getEstimatedFinishTime() - b.getEstimatedFinishTime();
            if (n == 0) {
                return b.getNumberOfTasks() - a.getNumberOfTasks();
            }
            return n;
        });
        visitedSchedules = new TreeSet<>((a, b) -> {
            int n = a.getEstimatedFinishTime() - b.getEstimatedFinishTime();
            if (n == 0) {
                int m = b.getNumberOfTasks() - a.getNumberOfTasks();
                if (m == 0) {
                    for (int i=0;i<tasks.length;i++) {
                        if (a.getTaskProcessorMap()[i] != b.getTaskProcessorMap()[i]) {
                            return a.getTaskProcessorMap()[i] - b.getTaskProcessorMap()[i];
                        }
                        if (a.getTaskStartTimeMap()[i] != b.getTaskStartTimeMap()[i]) {
                            return a.getTaskStartTimeMap()[i] - b.getTaskStartTimeMap()[i];
                        }
                    }
                    return 0;
                }
                return m;
            }
            return n;
        });
    }

    /**
     * Calculate the bottom, static and top levels of each task.
     */
    public void preprocess() {
        Preprocessor.calculateTaskStaticAndBottomLevels(taskBottomLevelMap, taskStaticLevelMap, tasks);
        Preprocessor.calculateTaskTopLevels(taskTopLevelMap, tasks);
        Preprocessor.calculateRequirements(tasks, taskRequirementsMap);
    }

    /**
     * Generates an optimal schedule using an A* algorithm.
     *
     * @return an optimal schedule
     */
    public Schedule findOptimalSchedule() {
        findFeasibleSchedule();

        // (1) OPEN priority queue, sorted by f
        generateInitialSchedules();

        while (scheduleQueue.size() != 0) {
            // (2) Remove from OPEN the search state s with the smallest f
            Schedule s = scheduleQueue.poll();

            // (3) If s is the goal state, a complete and optimal schedule is found and the algorithm stops;
            // otherwise, go to the next step.
            if (s.getNumberOfTasks() == tasks.length) {
                return s;
            }

            // (4) Expand the state s, which produces new state s'. Compute f and put s' into OPEN. Go to (2).
            expandSchedule(s);
        }

        return feasibleSchedule;
    }

    /**
     * Generates a valid schedule using a greedy approach.
     *
     * @return a valid schedule
     */
    public Schedule findFeasibleSchedule() {
        preprocess();

        Schedule schedule =
                new Schedule(tasks.length, processors, taskRequirementsMap.clone());
        Queue<Task> tasksToSchedule = new PriorityQueue<>(getTaskComparator());
        for (Integer i : schedule.getBeginnableTasks()) {
            tasksToSchedule.add(tasks[i]);
        }

        while (tasksToSchedule.size() > 0) {
            Task t = tasksToSchedule.poll();

            //We are finding the processor that allows for the earliest start time for task t
            int earliestStartTime = Integer.MAX_VALUE;
            int earliestStartProcessor = -1;
            for (int i = 0; i < processors; i++) {
                int start = calculateEarliestTimeToSchedule(schedule, t, i);
                if (start < earliestStartTime) {
                    earliestStartTime = start;
                    earliestStartProcessor = i;
                }
            }

            schedule.addTask(t, earliestStartProcessor, earliestStartTime);
            schedule.setEstimatedFinishTime(Math.max(schedule.getEstimatedFinishTime(), earliestStartTime + taskStaticLevelMap[t.getUniqueID()]));

            for (Edge e : t.getOutgoingEdges()) {
                if (schedule.isBeginnable(e.getHead())) {
                    tasksToSchedule.add(e.getHead());
                }
            }
        }
        feasibleSchedule = schedule;
        return feasibleSchedule;
    }

    /**
     * Expand the states by exhaustively matching all the ready
     * nodes to the processors. Each matching produces a new state s'.
     * Compute f(s') = g(s')+h(s') for each new state s'.
     * Put all the new states in OPEN.
     *
     * @param s
     */
    public void expandSchedule(Schedule s) {
        //Expanding the schedule s, and insert them into the scheduleQueue
        for (Integer t : s.getBeginnableTasks()) {
            int minDistanceToEnd = taskStaticLevelMap[tasks[t].getUniqueID()];

            boolean normalised = false;
            for (int i = 0; i < processors; i++) {
                if (calculateEarliestTimeToSchedule(s,tasks[t],i) == 0 && t>s.getNormalisationIndex()) {
                    if (normalised) {
                        continue;
                    }
                    normalised = true;
                }
                Schedule newSchedule = generateNewSchedule(s, tasks[t], minDistanceToEnd, i);

                //Only add the new schedule to the queue if it can potentially be better than the feasible schedule.
                if (newSchedule.getEstimatedFinishTime() < feasibleSchedule.getEstimatedFinishTime() && !visitedSchedules.contains(newSchedule)) {
                    scheduleQueue.add(newSchedule);
                    if (newSchedule.getEstimatedFinishTime() == s.getEstimatedFinishTime()) {
                        s.setPartialExpansionIndex(t.byteValue());
                        scheduleQueue.add(s);
                        return;
                    }
                }
            }
        }
        visitedSchedules.add(s);
    }

    /**
     * Generates a list of schedules (formally, States), each with one task scheduled that has no prerequisites.
     *
     * @return a list schedules, each with one task
     */
    private void generateInitialSchedules() {
        //Creating schedules for all the tasks that can be completed at the beginning (i.e. tasks which have no prerequisites)
        for (int i = 0; i < tasks.length; i++) {
            if (taskRequirementsMap[i] == 0) {
                Schedule s = new Schedule(tasks.length, processors, taskRequirementsMap.clone());
                s.addTask(tasks[i], 0, 0);
                s.setEstimatedFinishTime(taskStaticLevelMap[i]);
                scheduleQueue.add(s);
            }
        }
    }

    /**
     * On the given processor, find the earliest time we can schedule the given task
     *
     * @param schedule
     * @param task
     * @param processor
     * @return the earliest time that the task can be scheduled on the processor
     */
    private int calculateEarliestTimeToSchedule(Schedule schedule, Task task, int processor) {
        int earliestStartTime = schedule.getProcessorFinishTime(processor);
        byte[] taskProcessorMap = schedule.getTaskProcessorMap();
        for (Edge e : task.getIngoingEdges()) {
            int finishTime = schedule.getTaskFinishTime(e.getTail());
            if (taskProcessorMap[e.getTail().getUniqueID()] == processor) {
                earliestStartTime = Math.max(earliestStartTime, finishTime);
            } else {
                earliestStartTime = Math.max(earliestStartTime, finishTime + e.getWeight());
            }
        }
        return earliestStartTime;
    }

    /**
     * Creates a new schedule from schedule s which allocates task t to the given processor
     *
     * @param s
     * @param t
     * @param minDistanceToEnd
     * @param processor
     * @return the new schedule
     */
    private Schedule generateNewSchedule(Schedule s, Task t, int minDistanceToEnd, int processor) {
        Schedule newSchedule = s.clone();
        //Compute the earliest time to schedule the task ('t') on this processor (processor 'i')
        int earliestStartTime = calculateEarliestTimeToSchedule(newSchedule, t, processor);
        newSchedule.addTask(t, processor, earliestStartTime);
        int lowerBound = Math.max((newSchedule.getIdleTime() + totalComputationTime) / processors, //The schedule length is then bounded by the sum of total idle time and the total weight (execution time) of all nodes divided by the number of processors,
                Math.max(newSchedule.getEstimatedFinishTime(), earliestStartTime + minDistanceToEnd));  //Computation bottom level of task t

        for (Integer beginnable : newSchedule.getBeginnableTasks()) {
            Task task = tasks[beginnable];
            int nextEarliestStartTime = Integer.MAX_VALUE;
            for (int i = 0; i < processors; i++) {
                int start = calculateEarliestTimeToSchedule(newSchedule, task, i);
                if (start < nextEarliestStartTime) {
                    nextEarliestStartTime = start;
                }
            }
            lowerBound = Math.max(lowerBound, nextEarliestStartTime + taskStaticLevelMap[beginnable]);
        }

        newSchedule.setEstimatedFinishTime(lowerBound);
        return newSchedule;
    }

    /**
     * A comparator that sorts tasks in terms of their bottom and top levels.
     * The greater the sum of the bottom and top levels of a task, the higher its priority.
     *
     * @return a comparator to sort tasks
     */
    private Comparator<Task> getTaskComparator() {
        return (a, b) -> taskBottomLevelMap[b.getUniqueID()] + taskTopLevelMap[b.getUniqueID()]
                - taskBottomLevelMap[a.getUniqueID()] - taskTopLevelMap[a.getUniqueID()];
    }
}
