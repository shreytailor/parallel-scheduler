package com.team7;

import com.team7.model.Edge;
import com.team7.model.Schedule;
import com.team7.model.Task;

import java.util.*;

public class Scheduler {
    Map<Task, Integer> taskRequirementsMap = new HashMap<>();
    Set<Task> beginnableTasks = new HashSet<>();
    Map<Task, Integer> taskDistanceMap = new HashMap<>();

    public void preprocess(List<Task> tasks) {
        for (Task task : tasks) {
            if (task.getIngoingEdges().size() != 0) {
                taskRequirementsMap.put(task, task.getIngoingEdges().size());
            } else {
                beginnableTasks.add(task);
            }
        }
        for (Task task : tasks) {
            if (task.getOutgoingEdges().size() == 0) {
                taskDistanceMap.put(task, task.getWeight());
                Queue<Task> taskQueue = new LinkedList<>();
                taskQueue.add(task);
                while (taskQueue.size() > 0) {
                    Task t = taskQueue.poll();
                    for (Edge e : t.getIngoingEdges()) {
                        Task neighbour = e.getTail();
                        taskDistanceMap.compute(neighbour, (k,v) -> {
                            if (v==null) {
                                v=0;
                            }
                            return Math.max(v, taskDistanceMap.get(t)+neighbour.getWeight());
                        });
                        taskDistanceMap.put(neighbour, Math.max(taskDistanceMap.getOrDefault(neighbour, 0), taskDistanceMap.get(t) + neighbour.getWeight()));
                        taskQueue.add(neighbour);
                    }
                }
            }
        }
    }

    /**
     * Generates a list of schedules (formally, States), with one task scheduled that has no prerequisites.
     *
     * @param tasks
     * @param numProcessors
     * @return
     */
    private Queue<Schedule> generateInitialSchedules(List<Task> tasks, int numProcessors) {
        Queue<Schedule> scheduleQueue = new PriorityQueue<>(Comparator.comparingInt(Schedule::getEstimatedFinishTime));
        //Creating schedules for all the tasks that can be completed at the beginning (i.e. tasks which have no prerequisites)
        for (Task t : tasks) {
            if (t.getIngoingEdges().size() == 0) {
                beginnableTasks.remove(t);
                Schedule s = new Schedule(numProcessors, new HashMap<>(taskRequirementsMap), new HashSet<>(beginnableTasks));
                s.addTask(t, 0, 0);
                s.setEstimatedFinishTime(taskDistanceMap.get(t));
                scheduleQueue.add(s);
                beginnableTasks.add(t);
            }
        }
        return scheduleQueue;
    }

    public Schedule findOptimalSchedule(List<Task> tasks, int numProcessors) {
        preprocess(tasks);
        Queue<Schedule> scheduleQueue = generateInitialSchedules(tasks, numProcessors);

        while (scheduleQueue.size() != 0) {
            Schedule s = scheduleQueue.poll();

            //Check if the schedule is complete
            if (s.getNumberOfTasks() == tasks.size()) {
                return s;
            } else {
                Map<Task, Integer> taskProcessorMap = s.getTaskProcessorMap();
                //Get a list of tasks that haven't been allocated to the current schedule 's'
                Set<Task> tasksToSchedule = s.getBeginnableTasks();

                //Expanding the schedule s, and insert them into the scheduleQueue
                for (Task t : tasksToSchedule) {
                    int minDistanceToEnd = taskDistanceMap.get(t);
                    for (int i = 0; i < numProcessors; i++) {
                        Schedule newSchedule = s.clone();
                        //Compute the earliest time to schedule the task ('t') on this processor (processor 'i')
                        int earliestStartTime = getEarliestTimeToSchedule(taskProcessorMap, t, i, newSchedule);
                        newSchedule.addTask(t, i, earliestStartTime);
                        newSchedule.setEstimatedFinishTime(Math.max(newSchedule.getEstimatedFinishTime(), earliestStartTime+minDistanceToEnd));
                        scheduleQueue.add(newSchedule);
                    }
                }
            }
        }
        return null;
    }

    /**
     * On the given processor, find the earliest time we can schedule the given task
     *
     * @param taskProcessorMap
     * @param task
     * @param processor
     * @param newSchedule
     * @return
     */
    private int getEarliestTimeToSchedule(Map<Task, Integer> taskProcessorMap, Task task, int processor, Schedule newSchedule) {
        int earliestStartTime = newSchedule.getProcessorFinishTime(processor);
        for (Edge e : task.getIngoingEdges()) {
            int finishTime = newSchedule.getTaskFinishTime(e.getTail());
            if (taskProcessorMap.get(e.getTail()) == processor) {
                earliestStartTime = Math.max(earliestStartTime, finishTime);
            } else {
                earliestStartTime = Math.max(earliestStartTime, finishTime + e.getWeight());
            }
        }
        return earliestStartTime;
    }
}
