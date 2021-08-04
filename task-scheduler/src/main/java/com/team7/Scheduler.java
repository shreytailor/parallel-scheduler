package com.team7;

import com.team7.model.Edge;
import com.team7.model.Graph;
import com.team7.model.Schedule;
import com.team7.model.Task;

import java.util.*;

import static com.team7.algoutils.Preprocess.*;
import static com.team7.algoutils.ScheduleCalculator.getEarliestTimeToSchedule;

public class Scheduler {
    int processors;
    Task[] tasks;
    int[] taskTopLevelMap;
    int[] taskBottomLevelMap;
    int[] taskStaticLevelMap;
    byte[] taskRequirementsMap;
    Queue<Task> beginnableTasks = new PriorityQueue<>(getTaskComparator());

    Schedule feasibleSchedule;

    private Comparator<Task> getTaskComparator() {
        return (a, b) -> {
            return taskBottomLevelMap[b.getUniqueID()] + taskTopLevelMap[b.getUniqueID()] - taskBottomLevelMap[a.getUniqueID()] - taskTopLevelMap[a.getUniqueID()];
        };
    }

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

    public void preprocess() {
        calculateTaskStaticAndBottomLevels(taskBottomLevelMap, taskStaticLevelMap, tasks);
        calculateTaskTopLevels(taskTopLevelMap, tasks);
        calculateRequirements(tasks, taskRequirementsMap, beginnableTasks);
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

        // (1) OPEN priority queue, sorted by f
        Queue<Schedule> scheduleQueue = generateInitialSchedules();

        while (scheduleQueue.size() != 0) {
            // (2) Remove from OPEN the search state s with the smallest f
            Schedule s = scheduleQueue.poll();

            // (3) If s is the goal state, a complete and optimal schedule is found and the algorithm stops;
            // otherwise, go to the next step.
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


}
