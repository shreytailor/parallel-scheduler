package com.team7.algorithm;

import com.team7.Entrypoint;
import com.team7.model.Edge;
import com.team7.model.Graph;
import com.team7.model.Schedule;
import com.team7.model.Task;

import java.util.*;

public class Scheduler {
    protected int processors;
    protected Task[] tasks;
    protected int[] taskTopLevelWithoutEdgeCostsMap;
    protected int[] taskBottomLevelMap;
    protected int[] taskBottomLevelsWithoutEdgeCostsMap;
    protected byte[] taskRequirementsMap;
    protected List[] taskEquivalencesMap;
    protected Schedule feasibleSchedule;
    protected int totalComputationTime = 0;
    protected TreeSet<Schedule> openSchedules;
    protected TreeSet<Schedule> visitedSchedules;
    protected Schedule sharedState;

    public Scheduler(Graph g, int numOfProcessors) {
        processors = numOfProcessors;
        tasks = Preprocessor.getTopologicalOrder(g.getNodes());
        //Assigning a unique id to each task.
        for (int i = 0; i < tasks.length; i++) {
            Task t = tasks[i];
            t.setUniqueID((byte) i);
            totalComputationTime += t.getWeight();
        }
        taskRequirementsMap = Preprocessor.calculateRequirements(tasks);
        taskTopLevelWithoutEdgeCostsMap = Preprocessor.calculateTaskTopLevelsWithoutEdgeCosts(tasks);
        taskBottomLevelMap = Preprocessor.calculateTaskBottomLevels(tasks);
        taskBottomLevelsWithoutEdgeCostsMap = Preprocessor.calculateTaskBottomLevelsWithoutEdgeCosts(tasks);
        taskEquivalencesMap = Preprocessor.calculateEquivalentTasks(tasks);
        openSchedules = createScheduleSet();
        visitedSchedules = createScheduleSet();
    }

    /**
     * Create a treeset to hold the schedules in the CLOSED and OPEN list. Schedules are sorted in ascending estimated finish time.
     *
     * @return a set which will be used to store schedules.
     */
    protected TreeSet<Schedule> createScheduleSet() {
        return new TreeSet<>((a, b) -> {
            if (a.getEstimatedFinishTime() == b.getEstimatedFinishTime()) {
                if (b.getNumberOfTasks() == a.getNumberOfTasks()) {
                    for (Task t : tasks) {
                        if (a.getTaskStartTime(t) != b.getTaskStartTime(t)) {
                            return a.getTaskStartTime(t) - b.getTaskStartTime(t);
                        }
                        if (a.getTaskProcessor(t) != b.getTaskProcessor(t)) {
                            return a.getTaskProcessor(t) - b.getTaskProcessor(t);
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
     * @return A complete and optimal schedule
     */
    public Schedule findOptimalSchedule() {
        findFeasibleSchedule();

        // (1) OPEN priority queue, sorted by f
        generateInitialSchedules();
        while (openSchedules.size() != 0) {
            // (2) Remove from OPEN the search state s with the smallest f
            Schedule s = openSchedules.pollFirst();

            // (3) If s is the goal state, a complete and optimal schedule is found and the algorithm stops;
            // otherwise, go to the next step.
            if (s.getNumberOfTasks() == tasks.length) {
                Entrypoint.stopTimerLabel();
                sharedState = s;
                return s;
            }
            // (4) Expand the state s, which produces new state s'. Compute f and put s' into OPEN. Go to (2).
            expandSchedule(s, openSchedules);
        }
        Entrypoint.stopTimerLabel();
        sharedState = feasibleSchedule;
        return feasibleSchedule;
    }

    /**
     * Generates a valid schedule using a greedy approach.
     *
     * @return a complete and valid schedule
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
            schedule.setEstimatedFinishTime(Math.max(schedule.getEstimatedFinishTime(), earliestStartTime + taskBottomLevelsWithoutEdgeCostsMap[t.getUniqueID()]));

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
     * @param s         the schedule we want to expand
     * @param schedules the queue which we will add new schedules to
     */
    protected void expandSchedule(Schedule s, TreeSet<Schedule> schedules) {
        tryToFixTaskOrder(s);

        Queue<Integer> beginnableTasks;
        if (s.isTaskOrderFixed()) {
            beginnableTasks = new LinkedList<>();
            beginnableTasks.add(s.getFixedTaskOrder().remove());
        } else {
            beginnableTasks = s.getBeginnableTasks();
        }

        Set<Task> equivalent = new HashSet<>();
        while (beginnableTasks.size() > 0) {
            Integer t = beginnableTasks.poll();
            if (equivalent.contains(tasks[t])) {
                continue;
            }
            equivalent.addAll(taskEquivalencesMap[t]);

            boolean partiallyExpanded = false;
            boolean normalised = false;
            for (int i = 0; i < processors; i++) {
                int earliestStartTime = calculateEarliestTimeToSchedule(s, tasks[t], i);
                //Normalising
                if (earliestStartTime == 0) {
                    if (normalised) {
                        continue;
                    }
                    normalised = true;
                    if (t < s.getNormalisationIndex() && !s.isTaskOrderFixed()) {
                        continue;
                    }
                }

                Schedule newSchedule = generateNewSchedule(s, tasks[t], i, earliestStartTime);
                sharedState = newSchedule;

                //Only add the new schedule to the queue if it can potentially be better than the feasible schedule.
                if (newSchedule.getEstimatedFinishTime() < feasibleSchedule.getEstimatedFinishTime() &&
                        (!newSchedule.isEquivalentSchedulePruningEnabled() || !containsEquivalentSchedule(newSchedule, tasks[t])) &&
                        !visitedSchedules.contains(newSchedule) &&
                        !schedules.contains(newSchedule)) {
                    schedules.add(newSchedule);
                    if (newSchedule.getEstimatedFinishTime() == s.getEstimatedFinishTime() && beginnableTasks.size() > 0) {
                        partiallyExpanded = true;
                    }
                }
            }
            if (partiallyExpanded) {
                s.setPartialExpansionIndex(t.byteValue());
                schedules.add(s);
                return;
            }
        }
        addToVisitedSchedules(s);
    }

    /**
     * Adds the schedule to the CLOSED list
     *
     * @param s the schedule we are adding
     */
    protected void addToVisitedSchedules(Schedule s) {
        visitedSchedules.add(s);
    }

    /**
     * Generates a list of schedules (formally, States), each with one task scheduled that has no prerequisites.
     */
    protected void generateInitialSchedules() {
        //Creating schedules for all the tasks that can be completed at the beginning (i.e. tasks which have no prerequisites)
        for (int i = 0; i < tasks.length; i++) {
            if (taskRequirementsMap[i] == 0) {
                Schedule s = new Schedule(tasks.length, processors, taskRequirementsMap.clone());
                s.addTask(tasks[i], 0, 0);
                s.setEstimatedFinishTime(taskBottomLevelsWithoutEdgeCostsMap[i]);
                openSchedules.add(s);
            }
        }
    }

    /**
     * On the given processor, find the earliest time we can schedule the given task
     *
     * @param schedule  the schedule we want to add the task to
     * @param processor the processor which we want to schedule the task on
     * @param task      the task we want to schedule
     * @return the earliest time we can schedule the task
     */
    protected int calculateEarliestTimeToSchedule(Schedule schedule, Task task, int processor) {
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

    /**
     * Similar to the method above but uses information from the provided arrays and not the whole Schedule object.
     *
     * @param processor        the processor we want to schedule the task on
     * @param t1               the task to schedule
     * @param t1StartTime      the earliest time that we can schedule t1 that we know so far
     * @param taskProcessorMap an array representing the task processor allocations
     * @param taskStartTimes   an array representing the task start times
     * @return the earliest time we can schedule the task on the given processor
     */
    private int calculateEarliestTimeToSchedule(int[] taskStartTimes, byte[] taskProcessorMap, Task t1, int processor, int t1StartTime) {
        int earliestStartTime = t1StartTime;
        for (Edge e : t1.getIngoingEdges()) {
            Task parent = e.getTail();
            int finishTime = taskStartTimes[parent.getUniqueID()] + parent.getWeight();
            if (taskProcessorMap[parent.getUniqueID()] == processor) {
                earliestStartTime = Math.max(earliestStartTime, finishTime);
            } else {
                earliestStartTime = Math.max(earliestStartTime, finishTime + e.getWeight());
            }
        }
        return earliestStartTime;
    }

    /**
     * Creates a new schedule from schedule s which allocates task t to the given processor
     */
    protected Schedule generateNewSchedule(Schedule s, Task t, int processor, int earliestStartTime) {
        Schedule newSchedule = s.clone();
        newSchedule.addTask(t, processor, earliestStartTime);

        int lowerBound = Math.max((newSchedule.getIdleTime() + totalComputationTime) / processors, //The schedule length is then bounded by the sum of total idle time and the total weight (execution time) of all nodes divided by the number of processors,
                Math.max(newSchedule.getEstimatedFinishTime(), earliestStartTime + taskBottomLevelsWithoutEdgeCostsMap[t.getUniqueID()]));  //Computation bottom level of task t
        for (Integer beginnable : newSchedule.getBeginnableTasks()) {
            Task task = tasks[beginnable];
            int nextEarliestStartTime = Integer.MAX_VALUE;
            for (int i = 0; i < processors; i++) {
                int start = calculateEarliestTimeToSchedule(newSchedule, task, i);
                if (start < nextEarliestStartTime) {
                    nextEarliestStartTime = start;
                }
            }
            lowerBound = Math.max(lowerBound, nextEarliestStartTime + taskBottomLevelsWithoutEdgeCostsMap[beginnable]);
        }
        newSchedule.setEstimatedFinishTime(lowerBound);
        return newSchedule;
    }

    /**
     * Checks if the provided schedule with a recently added task can have its tasks rearranged yet still
     * provide a schedule with the same or better estimated finish time.
     *
     * @param schedule  the Schedule
     * @param addedTask the Task that was just added to the schedule
     * @return boolean representing whether the schedule contains an equivalent schedule
     */
    private boolean containsEquivalentSchedule(Schedule schedule, Task addedTask) {
        int maxFinishTime = schedule.getTaskFinishTime(addedTask);
        int processor = schedule.getTaskProcessor(addedTask);

        //Get all the tasks on the same processor as addedTask
        List<Task> processorTasks = new ArrayList<>();
        for (Task t : tasks) {
            if (schedule.getTaskProcessor(t) == processor) {
                processorTasks.add(t);
            }
        }
        processorTasks.sort(Comparator.comparingInt(schedule::getTaskStartTime));

        int[] original = schedule.getTaskStartTimeMap();
        int[] taskStartTimes = schedule.getTaskStartTimeMap().clone();
        byte[] taskProcessorMap = schedule.getTaskProcessorMap();

        for (int i = processorTasks.size() - 1; i > 0; i--) {
            Task t1 = processorTasks.get(i);
            Task t2 = processorTasks.get(i - 1);

            if (t2.getUniqueID() < t1.getUniqueID()) {
                break;
            }

            //swapping the task order
            processorTasks.set(i - 1, t1);
            processorTasks.set(i, t2);

            int t1StartTime = 0;
            if (i > 1) {
                t1StartTime = taskStartTimes[processorTasks.get(i - 2).getUniqueID()] + processorTasks.get(i - 2).getWeight();
            }
            taskStartTimes[t1.getUniqueID()] = calculateEarliestTimeToSchedule(taskStartTimes, taskProcessorMap, t1, processor, t1StartTime);

            for (int j = i; j < processorTasks.size(); j++) {
                Task current = processorTasks.get(j);
                Task prev = processorTasks.get(j - 1);
                int currentStartTime = taskStartTimes[prev.getUniqueID()] + prev.getWeight();
                taskStartTimes[current.getUniqueID()] = calculateEarliestTimeToSchedule(taskStartTimes, taskProcessorMap, current, processor, currentStartTime);
            }
            if (taskStartTimes[processorTasks.get(processorTasks.size() - 1).getUniqueID()] + processorTasks.get(processorTasks.size() - 1).getWeight() <= maxFinishTime &&
                    areChildrenStillValid(i, processorTasks, taskStartTimes, original, taskProcessorMap, processor, schedule.getProcessorFinishTimes())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks that after rearranging the tasks on the specific processor, the children of those tasks are unaffected.
     *
     * @return boolean representing whether the children are valid or not
     */
    private boolean areChildrenStillValid(int i, List<Task> processorTasks, int[] taskStartTimes, int[] originalTaskStartTimes, byte[] taskProcessorMap, int processor, int[] processorFinishTimes) {
        for (int k = i; k < processorTasks.size(); k++) {
            Task task = processorTasks.get(k);
            if (taskStartTimes[task.getUniqueID()] > originalTaskStartTimes[task.getUniqueID()]) {
                for (Edge e : task.getOutgoingEdges()) {
                    int earliest = taskStartTimes[task.getUniqueID()] + task.getWeight() + e.getWeight();
                    Task child = e.getHead();
                    if (taskProcessorMap[child.getUniqueID()] != -1) {
                        if (taskProcessorMap[child.getUniqueID()] != processor && earliest > taskStartTimes[child.getUniqueID()]) {
                            return false;
                        }
                    } else {
                        for (int p = 0; p < processors; p++) {
                            if (p != processor) {
                                boolean scheduleLater = false;
                                for (Edge in : child.getIngoingEdges()) {
                                    Task parent = in.getTail();
                                    if (parent != task) {
                                        if (taskProcessorMap[parent.getUniqueID()] == -1) {
                                            int parentStartTime = Integer.MAX_VALUE;
                                            for (int q = 0; q < processor; q++) {
                                                if (q != p) {
                                                    parentStartTime = Math.min(parentStartTime, processorFinishTimes[q]);
                                                }
                                            }
                                            parentStartTime = Math.max(parentStartTime, taskTopLevelWithoutEdgeCostsMap[parent.getUniqueID()]);
                                            if (parentStartTime + parent.getWeight() + in.getWeight() >= earliest) {
                                                scheduleLater = true;
                                                break;
                                            }
                                        } else {
                                            int communicationCost = 0;
                                            if (taskProcessorMap[parent.getUniqueID()] != p) {
                                                communicationCost = in.getWeight();
                                            }
                                            if (taskStartTimes[parent.getUniqueID()] + parent.getWeight() + communicationCost >= earliest) {
                                                scheduleLater = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (!scheduleLater) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Tries to generate a fixed task order for a schedule
     *
     * @param schedule the schedule for which we are trying to fix the task order.
     */
    private void tryToFixTaskOrder(Schedule schedule) {
        if (schedule.isTaskOrderFixed() || schedule.isPartiallyExpanded()) {
            return;
        }

        Queue<Integer> taskIDs = schedule.getBeginnableTasks();
        if (taskIDs.size() <= 1 || !satisfiesFixingOrderCondition(taskIDs, schedule)) {
            return;
        }

        LinkedList<Integer> fixedTaskOrder = createFixedTaskOrderList(taskIDs, schedule);

        if (isValidFixedTaskOrder(fixedTaskOrder)) {
            schedule.fixTaskOrder(fixedTaskOrder);
        }
    }

    /**
     * Checks whether the provided list of free tasks can have their order fixed.
     *
     * @param taskIDs  the free tasks that we are checking
     * @param schedule the schedule from which these tasks originate
     * @return
     */
    private boolean satisfiesFixingOrderCondition(Queue<Integer> taskIDs, Schedule schedule) {
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

    /**
     * Generates a fixed task order list from the provided tasks.
     *
     * @param taskIDs  the free tasks that we are fixing
     * @param schedule the schedule that we are fixing the task order for
     * @return a list containing the fixed task order
     */
    private LinkedList<Integer> createFixedTaskOrderList(Queue<Integer> taskIDs, Schedule schedule) {
        LinkedList<Integer> fixedTaskOrder = new LinkedList<>(taskIDs);
        fixedTaskOrder.sort((a, b) -> {
            int DRTa = 0;
            if (tasks[a].getIngoingEdges().size() == 1) {
                Edge ingoing = tasks[a].getIngoingEdges().get(0);
                DRTa = schedule.getTaskFinishTime(ingoing.getTail()) + ingoing.getWeight();
            }
            int DRTb = 0;
            if (tasks[b].getIngoingEdges().size() == 1) {
                Edge ingoing = tasks[b].getIngoingEdges().get(0);
                DRTb = schedule.getTaskFinishTime(ingoing.getTail()) + ingoing.getWeight();
            }
            if (DRTa == DRTb) {
                int outCosta = 0;
                if (tasks[a].getOutgoingEdges().size() == 1) {
                    outCosta = tasks[a].getOutgoingEdges().get(0).getWeight();
                }
                int outCostb = 0;
                if (tasks[b].getOutgoingEdges().size() == 1) {
                    outCostb = tasks[b].getOutgoingEdges().get(0).getWeight();
                }
                return outCostb - outCosta;
            }
            return DRTa - DRTb;
        });
        return fixedTaskOrder;
    }

    /**
     * Verifies that the fixed task order satisfies the condition where free tasks are in non-increasing out-edge cost order.
     *
     * @param fixedTaskOrder the task order that we are checking
     * @return true if the ordering is valid otherwise return false
     */
    private boolean isValidFixedTaskOrder(LinkedList<Integer> fixedTaskOrder) {
        Integer prev = 0;
        if (tasks[fixedTaskOrder.peek()].getOutgoingEdges().size() == 1) {
            prev = tasks[fixedTaskOrder.peek()].getOutgoingEdges().get(0).getWeight();
        }
        for (Integer i : fixedTaskOrder) {
            int outEdgeCost = 0;
            if (tasks[i].getOutgoingEdges().size() == 1) {
                outEdgeCost = tasks[i].getOutgoingEdges().get(0).getWeight();
            }
            if (outEdgeCost > prev) {
                return false;
            }
            prev = outEdgeCost;
        }
        return true;
    }

    public Task[] getTasks() {
        return tasks;
    }

    public Schedule getSharedState() {
        return sharedState;
    }

    /**
     * Open state just means it's a state that is to be expanded
     *
     * @return the number of states waiting to be expanded
     */
    public int getInfoOpenStates() {
        return openSchedules.size();
    }

    /**
     * Closed state means it has been expanded and won't be looked at again
     *
     * @return number of closed states
     */
    public int getInfoClosedStates() {
        return visitedSchedules.size();
    }
}
