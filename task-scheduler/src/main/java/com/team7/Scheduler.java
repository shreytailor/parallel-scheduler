package com.team7;

import com.team7.model.Edge;
import com.team7.model.Graph;
import com.team7.model.Schedule;
import com.team7.model.Task;

import javax.xml.bind.SchemaOutputResolver;
import java.util.*;

public class Scheduler {
    int processors;
    Task[] tasks;
    int[] taskTopLevelMap;
    int[] taskBottomLevelMap;
    int[] taskStaticLevelMap;
    byte[] taskRequirementsMap;
    Queue<Task> beginnableTasks = new PriorityQueue<>((a, b) -> taskBottomLevelMap[b.getUniqueID()] + taskTopLevelMap[b.getUniqueID()] - taskBottomLevelMap[a.getUniqueID()] - taskTopLevelMap[a.getUniqueID()]);
    Schedule feasibleSchedule;

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

    public void calculateTaskStaticAndBottomLevels() {
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

    public void calculateTaskTopLevels() {
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

    public void preprocess() {
        calculateTaskStaticAndBottomLevels();
        calculateTaskTopLevels();

        for (Task task : tasks) {
            if (task.getIngoingEdges().size() != 0) {
                taskRequirementsMap[task.getUniqueID()] = (byte) task.getIngoingEdges().size();
            } else {
                beginnableTasks.add(task);
            }
        }
    }

    public Schedule findFeasibleSchedule() {
        Schedule schedule = new Schedule(tasks.length, processors, taskRequirementsMap.clone(), new PriorityQueue<>(beginnableTasks));
        Queue<Task> tasksToSchedule = schedule.getBeginnableTasks();
        while (tasksToSchedule.size() > 0) {
            Task t = tasksToSchedule.poll();
            int earliestStartTime = Integer.MAX_VALUE;
            int earliestStartProcessor = -1;
            for (int i = 0; i < processors; i++) {
                int start = getEarliestTimeToSchedule(schedule, t, i);
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
     * Generates a list of schedules (formally, States), with one task scheduled that has no prerequisites.
     */
    private Queue<Schedule> generateInitialSchedules() {
        Queue<Schedule> scheduleQueue = new PriorityQueue<>(Comparator.comparingInt(Schedule::getEstimatedFinishTime));
        //Creating schedules for all the tasks that can be completed at the beginning (i.e. tasks which have no prerequisites)
        for (Task t : tasks) {
            if (t.getIngoingEdges().size() == 0) {
                beginnableTasks.remove(t);
                Schedule s = new Schedule(tasks.length, processors, taskRequirementsMap.clone(), new PriorityQueue<>(beginnableTasks));
                s.addTask(t, 0, 0);
                s.setEstimatedFinishTime(taskStaticLevelMap[t.getUniqueID()]);
                scheduleQueue.add(s);
                beginnableTasks.add(t);
            }
        }
        return scheduleQueue;
    }

    public Schedule findOptimalSchedule() {
        preprocess();
        findFeasibleSchedule();

        Queue<Schedule> scheduleQueue = generateInitialSchedules();
        while (scheduleQueue.size() != 0) {
            Schedule s = scheduleQueue.poll();

            //Check if the schedule is complete
            if (s.getNumberOfTasks() == tasks.length) {
                return s;
            } else {
                //Get a list of tasks that haven't been allocated to the current schedule 's'
                Queue<Task> tasksToSchedule = s.getBeginnableTasks();
                List<Task> copy = new ArrayList<>(tasksToSchedule);
                //Expanding the schedule s, and insert them into the scheduleQueue
                for (Task t : copy) {
                    tasksToSchedule.remove(t);
                    int minDistanceToEnd = taskStaticLevelMap[t.getUniqueID()];
                    for (int i = 0; i < processors; i++) {
                        Schedule newSchedule = s.clone();
                        //Compute the earliest time to schedule the task ('t') on this processor (processor 'i')
                        int earliestStartTime = getEarliestTimeToSchedule(newSchedule, t, i);
                        newSchedule.addTask(t, i, earliestStartTime);
                        newSchedule.setEstimatedFinishTime(Math.max(newSchedule.getEstimatedFinishTime(), earliestStartTime + minDistanceToEnd));
                        if (newSchedule.getEstimatedFinishTime() < feasibleSchedule.getEstimatedFinishTime()) {
                            scheduleQueue.add(newSchedule);
                        }
                    }
                    tasksToSchedule.add(t);
                }
            }
        }
        return feasibleSchedule;
    }

    /**
     * On the given processor, find the earliest time we can schedule the given task
     *
     * @param schedule
     * @param task
     * @param processor
     * @return
     */
    private int getEarliestTimeToSchedule(Schedule schedule, Task task, int processor) {
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
}
