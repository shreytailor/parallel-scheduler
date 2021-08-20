package com.team7.algorithm;

import com.team7.model.Edge;
import com.team7.model.Graph;
import com.team7.model.Schedule;
import com.team7.model.Task;
import com.team7.visualization.system.TimeProvider;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.*;

public class Scheduler {
    protected int processors;
    protected Task[] tasks;
    protected int[] taskTopLevelMap;
    protected int[] taskBottomLevelMap;
    protected int[] taskStaticLevelMap;
    protected byte[] taskRequirementsMap;
    protected List[] taskEquivalencesMap;
    protected Schedule feasibleSchedule;
    protected int totalComputationTime = 0;
    protected Queue<Schedule> scheduleQueue;
    protected Set<Schedule> visitedSchedules;
    protected Schedule sharedState;

    /**
     * Open state just means it's a state that is to be expanded
     */
    public int getInfoOpenStates() {
        return scheduleQueue.size();
    }

    /**
     * Closed state means it has been expanded and won't be looked at again
     * @return number of closed states
     */
    public int getInfoClosedStates() {
        return visitedSchedules.size();
    }

    public Scheduler(Graph g, int numOfProcessors) {
        processors = numOfProcessors;
        tasks = Preprocessor.getTopologicalOrder(g.getNodes());
        for (int i = 0; i < tasks.length; i++) {
            Task t = tasks[i];
            t.setUniqueID((byte) i);
            totalComputationTime += t.getWeight();
        }
        taskRequirementsMap = Preprocessor.calculateRequirements(tasks);
        taskTopLevelMap = Preprocessor.calculateTaskTopLevels(tasks);
        taskBottomLevelMap = Preprocessor.calculateTaskBottomLevels(tasks);
        taskStaticLevelMap = Preprocessor.calculateTaskStaticLevels(tasks);
        taskEquivalencesMap = Preprocessor.calculateEquivalentTasks(tasks);
        scheduleQueue = createScheduleQueue();
        visitedSchedules = createVisitedScheduleSet();
    }

    public static Queue<Schedule> createScheduleQueue() {
        return new PriorityQueue<>((a, b) -> {
            int n = a.getEstimatedFinishTime() - b.getEstimatedFinishTime();
            if (n == 0) {
                return b.getNumberOfTasks() - a.getNumberOfTasks();
            }
            return n;
        });
    }

    protected Set<Schedule> createVisitedScheduleSet() {
        return new TreeSet<>((a, b) -> {
            if (a.getEstimatedFinishTime() == b.getEstimatedFinishTime()) {
                if (b.getNumberOfTasks() == a.getNumberOfTasks()) {
                    for (Task t : tasks) {
                        if (a.getTaskProcessor(t) != b.getTaskProcessor(t)) {
                            return a.getTaskProcessor(t) - b.getTaskProcessor(t);
                        }
                        if (a.getTaskStartTime(t) != b.getTaskStartTime(t)) {
                            return a.getTaskStartTime(t) - b.getTaskStartTime(t);
                        }
                    }
                    return 0;
                }
                return b.getNumberOfTasks() - a.getNumberOfTasks();
            }
            return a.getEstimatedFinishTime() - b.getEstimatedFinishTime();
        });
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
                TimeProvider.getInstance().stopTimerLabel();
                return s;
            }
            // (4) Expand the state s, which produces new state s'. Compute f and put s' into OPEN. Go to (2).
            expandSchedule(s);

        }
        TimeProvider.getInstance().stopTimerLabel();
        return feasibleSchedule;
    }

    /**
     * Generates a valid schedule using a greedy approach.
     *
     * @return a valid schedule
     */
    public Schedule findFeasibleSchedule() {
        Schedule schedule =
                new Schedule(tasks.length, processors, taskRequirementsMap.clone());
        Queue<Task> tasksToSchedule = new PriorityQueue<>((a, b) -> taskBottomLevelMap[b.getUniqueID()] - taskBottomLevelMap[a.getUniqueID()]);
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
        Set<Task> equivalent = new HashSet<>();
        for (Integer t : s.getBeginnableTasks()) {
            if (equivalent.contains(tasks[t])) {
                continue;
            }
            equivalent.addAll(taskEquivalencesMap[t]);
            boolean normalised = false;
            for (int i = 0; i < processors; i++) {
                int earliestStartTime = calculateEarliestTimeToSchedule(s, tasks[t], i);
                if (earliestStartTime == 0) {
                    if (normalised || t < s.getNormalisationIndex()) {
                        continue;
                    }
                    normalised = true;
                }

                Schedule newSchedule = generateNewSchedule(s, tasks[t], i, earliestStartTime);
                sharedState = newSchedule;

                // Only add the new schedule to the queue if it can potentially be better than the feasible schedule.
                if (newSchedule.getEstimatedFinishTime() < feasibleSchedule.getEstimatedFinishTime() &&
                        !containsEquivalentSchedule(newSchedule, tasks[t]) &&
                        !visitedSchedules.contains(newSchedule)) {
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
    protected void generateInitialSchedules() {
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
    public int calculateEarliestTimeToSchedule(Schedule schedule, Task task, int processor) {
        int earliestStartTime = schedule.getProcessorFinishTime(processor);
        for (Edge e : task.getIngoingEdges()) {
            int finishTime = schedule.getTaskFinishTime(e.getTail());
            if (schedule.getTaskProcessor(e.getTail()) == processor) {
                earliestStartTime = Math.max(earliestStartTime, finishTime);
            } else {
                earliestStartTime = Math.max(earliestStartTime, finishTime + e.getWeight());
            }
        }
        return earliestStartTime;
    }

    private int calculateEarliestTimeToSchedule(int[] taskStartTimes, byte[] taskProcessorMap, Task t1, int processor, int t1StartTime) {
        int earliestStartTime = t1StartTime;
        for (Edge e : t1.getIngoingEdges()) {
            Task tail = e.getTail();
            int finishTime = taskStartTimes[tail.getUniqueID()] + tail.getWeight();
            if (taskProcessorMap[tail.getUniqueID()] == processor) {
                earliestStartTime = Math.max(t1StartTime, finishTime);
            } else {
                earliestStartTime = Math.max(t1StartTime, finishTime + e.getWeight());
            }
        }
        return earliestStartTime;
    }

    /**
     * Creates a new schedule from schedule s which allocates task t to the given processor
     *
     * @param s
     * @param t
     * @param processor
     * @param earliestStartTime
     * @return the new schedule
     */
    public Schedule generateNewSchedule(Schedule s, Task t, int processor, int earliestStartTime) {
        Schedule newSchedule = s.clone();
        newSchedule.addTask(t, processor, earliestStartTime);

        int lowerBound = Math.max((newSchedule.getIdleTime() + totalComputationTime) / processors, //The schedule length is then bounded by the sum of total idle time and the total weight (execution time) of all nodes divided by the number of processors,
                Math.max(newSchedule.getEstimatedFinishTime(), earliestStartTime + taskStaticLevelMap[t.getUniqueID()]));  //Computation bottom level of task t
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

    public boolean containsEquivalentSchedule(Schedule schedule, Task addedTask) {
        int maxFinishTime = schedule.getTaskFinishTime(addedTask);
        int processor = schedule.getTaskProcessor(addedTask);
        List<Task> taskSet = new ArrayList<>();
        for (Task t : tasks) {
            if (schedule.getTaskProcessor(t) == processor) {
                taskSet.add(t);
            }
        }
        taskSet.sort((a, b) -> schedule.getTaskStartTime(b) - schedule.getTaskStartTime(a));

        int[] original = schedule.getTaskStartTimeMap();
        int[] taskStartTimes = schedule.getTaskStartTimeMap().clone();
        byte[] taskProcessorMap = schedule.getTaskProcessorMap();
        for (int i = 1; i < taskSet.size(); i++) {
            Task t1 = taskSet.get(i - 1);
            Task t2 = taskSet.get(i);
            if (t2.getUniqueID() < t1.getUniqueID()) {
                break;
            }

            int t1StartTime = 0;
            if (i < taskSet.size() - 1) {
                t1StartTime = taskStartTimes[taskSet.get(i + 1).getUniqueID()] + taskSet.get(i + 1).getWeight();
            }
            taskStartTimes[t1.getUniqueID()] = calculateEarliestTimeToSchedule(taskStartTimes, taskProcessorMap, t1, processor, t1StartTime);

            for (int j = i; j > 0; j--) {
                Task current = taskSet.get(j);
                Task prev = taskSet.get(j - 1);
                int currentStartTime = taskStartTimes[prev.getUniqueID()] + prev.getWeight();
                taskStartTimes[current.getUniqueID()] = calculateEarliestTimeToSchedule(taskStartTimes, taskProcessorMap, current, processor, currentStartTime);
            }

            if (taskStartTimes[taskSet.get(1).getUniqueID()] + taskSet.get(1).getWeight() <= maxFinishTime) {
                boolean equivalent = true;
                for (int k = i; k > 0; k--) {
                    Task task = taskSet.get(k);
                    if (taskStartTimes[task.getUniqueID()] > original[task.getUniqueID()]) {
                        for (Edge e : task.getOutgoingEdges()) {
                            int earliest = taskStartTimes[task.getUniqueID()] + task.getWeight() + e.getWeight();
                            Task child = e.getHead();
                            if (taskProcessorMap[child.getUniqueID()] != -1) {
                                if (taskProcessorMap[child.getUniqueID()] != processor && earliest > taskStartTimes[child.getUniqueID()]) {
                                    equivalent = false;
                                }
                            } else {
                                for (int p = 0; p < processors; p++) {
                                    if (p != processor) {
                                        boolean scheduleLater = false;
                                        for (Edge in : child.getIngoingEdges()) {
                                            Task parent = in.getTail();
                                            if (taskStartTimes[parent.getUniqueID()] + in.getWeight() > earliest) {
                                                scheduleLater = true;
                                                break;
                                            }
                                        }
                                        if (!scheduleLater) {
                                            equivalent = false;
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
                if (equivalent) {
                    return true;
                }
            }
        }

        return false;
    }

    //Currently not in use
    public boolean canFixTaskOrder(Schedule schedule) {
        Queue<Integer> taskIDs = schedule.getBeginnableTasks();
        Task sharedChild = null;
        int sharedParentProcessor = -1;
        for (Integer i : taskIDs) {
            Task t = tasks[i];
            if (t.getIngoingEdges().size() > 1 || t.getOutgoingEdges().size() > 1) {
                return false;
            }
            if (t.getOutgoingEdges().size() == 1) {
                if (sharedChild == null) {
                    sharedChild = t.getOutgoingEdges().get(0).getHead();
                } else if (sharedChild != t.getOutgoingEdges().get(0).getHead()) {
                    return false;
                }
            }
            if (t.getIngoingEdges().size() == 1) {
                int parentProcessor = schedule.getTaskProcessor(t.getIngoingEdges().get(0).getTail());
                if (sharedParentProcessor == -1) {
                    sharedParentProcessor = parentProcessor;
                } else if (sharedParentProcessor != parentProcessor) {
                    return false;
                }
            }
        }
        return true;
    }

    public Task[] getTasks() {
        return tasks;
    }

    public int fixedTaskOrderCompare(Task a, Task b, Schedule s) {
        if (a == null) {
            return 1;
        }
        int DRTa = 0;
        if (a.getIngoingEdges().size() == 1) {
            Edge ingoing = a.getIngoingEdges().get(0);
            DRTa = s.getTaskFinishTime(ingoing.getTail()) + ingoing.getWeight();
        }
        int DRTb = 0;
        if (b.getIngoingEdges().size() == 1) {
            Edge ingoing = b.getIngoingEdges().get(0);
            DRTb = s.getTaskFinishTime(ingoing.getTail()) + ingoing.getWeight();
        }
        if (DRTa == DRTb) {
            int outCosta = 0;
            if (a.getOutgoingEdges().size() == 1) {
                outCosta = a.getOutgoingEdges().get(0).getWeight();
            }
            int outCostb = 0;
            if (b.getOutgoingEdges().size() == 1) {
                outCostb = b.getOutgoingEdges().get(0).getWeight();
            }
            return outCostb - outCosta;
        }
        return DRTa - DRTb;
    }

    public Schedule getSharedState() {
        return sharedState;
    }
}
