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
    private Queue<Task> beginnableTasks = new PriorityQueue<>(getTaskComparator());
    private Schedule feasibleSchedule;

    public Scheduler(Graph g, int numOfProcessors) {
        processors = numOfProcessors;
        int numTasks = g.getNodes().size();
        tasks = new Task[numTasks];
        for (Task t : g.getNodes()) {
            tasks[t.getUniqueID()] = t;
        }
        taskTopLevelMap = new int[numTasks];
        taskBottomLevelMap = new int[numTasks];
        taskStaticLevelMap = new int[numTasks];
        taskRequirementsMap = new byte[numTasks];
    }

    /**
     * Calculate the bottom, static and top levels of each task.
     */
    public void preprocess() {
        Preprocessor.calculateTaskStaticAndBottomLevels(taskBottomLevelMap, taskStaticLevelMap, tasks);
        Preprocessor.calculateTaskTopLevels(taskTopLevelMap, tasks);
        Preprocessor.calculateRequirements(tasks, taskRequirementsMap, beginnableTasks);
    }

    /**
     * Generates an optimal schedule using an A* algorithm.
     *
     * @return an optimal schedule
     */
    public Schedule findOptimalSchedule() {
        findFeasibleSchedule();

        // (1) OPEN priority queue, sorted by f
        Queue<Schedule> scheduleQueue =
                generateInitialSchedules(tasks, processors, taskRequirementsMap, beginnableTasks, taskStaticLevelMap, tasks.length);

        while (scheduleQueue.size() != 0) {
            // (2) Remove from OPEN the search state s with the smallest f
            Schedule s = scheduleQueue.poll();

            // (3) If s is the goal state, a complete and optimal schedule is found and the algorithm stops;
            // otherwise, go to the next step.
            if (s.getNumberOfTasks() == tasks.length) {
                return s;
            }

            // (4) Expand the state s, which produces new state s'. Compute f and put s' into OPEN. Go to (2).
            expandSchedule(scheduleQueue, s);
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
                new Schedule(tasks.length, processors, taskRequirementsMap.clone(), new PriorityQueue<>(beginnableTasks));
        Queue<Task> tasksToSchedule = schedule.getBeginnableTasks();
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
        }
        feasibleSchedule = schedule;
        return feasibleSchedule;
    }

    /**
     * Expand the states by exhaustively matching all the ready
     * nodes to the processors. Each matching produces a new state s'.
     * Compute f(s') = g(s')+h(s') for each new state s'.
     * Put all the new states in OPEN.
     * @param scheduleQueue
     * @param s
     */
    public void expandSchedule(Queue<Schedule> scheduleQueue, Schedule s) {
        //Get a list of tasks that haven't been allocated to the current schedule 's'
        Queue<Task> tasksToSchedule = s.getBeginnableTasks();
        List<Task> copy = new ArrayList<>(tasksToSchedule);

        //Expanding the schedule s, and insert them into the scheduleQueue
        for (Task t : copy) {
            tasksToSchedule.remove(t);
            int minDistanceToEnd = taskStaticLevelMap[t.getUniqueID()];
            for (int i = 0; i < processors; i++) {
                Schedule newSchedule = generateNewSchedule(s, t, minDistanceToEnd, i);
                //Only add the new schedule to the queue if it can potentially be better than the feasible schedule.
                if (newSchedule.getEstimatedFinishTime() < feasibleSchedule.getEstimatedFinishTime()) {
                    scheduleQueue.add(newSchedule);
                }
            }
            tasksToSchedule.add(t);
        }
    }

    /**
     * Generates a list of schedules (formally, States), each with one task scheduled that has no prerequisites.
     *
     * @param tasks
     * @param processors
     * @param taskRequirementsMap
     * @param beginnableTasks
     * @param taskStaticLevelMap
     * @param length
     * @return a list schedules, each with one task
     */
    private Queue<Schedule> generateInitialSchedules(Task[] tasks, int processors, byte[] taskRequirementsMap, Queue<Task> beginnableTasks, int[] taskStaticLevelMap, int length) {
        Queue<Schedule> scheduleQueue = new PriorityQueue<>(Comparator.comparingInt(Schedule::getEstimatedFinishTime));

        //Creating schedules for all the tasks that can be completed at the beginning (i.e. tasks which have no prerequisites)
        for (Task t : tasks) {
            if (t.getIngoingEdges().size() == 0) {
                beginnableTasks.remove(t);
                Schedule s = new Schedule(length, processors, taskRequirementsMap.clone(), new PriorityQueue<>(beginnableTasks));
                s.addTask(t, 0, 0);
                s.setEstimatedFinishTime(taskStaticLevelMap[t.getUniqueID()]);
                scheduleQueue.add(s);
                beginnableTasks.add(t);
            }
        }
        return scheduleQueue;
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
        newSchedule.setEstimatedFinishTime(Math.max(newSchedule.getEstimatedFinishTime(), earliestStartTime + minDistanceToEnd));
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
    
    public Task[] getTasks() {
        return tasks;
    }
}
