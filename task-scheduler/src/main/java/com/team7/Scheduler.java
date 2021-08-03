package com.team7;

import com.team7.model.Edge;
import com.team7.model.Schedule;
import com.team7.model.Task;

import javax.xml.bind.SchemaOutputResolver;
import java.util.*;

public class Scheduler {
    Map<Task, Integer> taskRequirementsMap = new HashMap<>();
    Map<Task, Integer> taskStaticLevelMap = new HashMap<>();
    Map<Task, Integer> taskBottomLevelMap = new HashMap<>();
    Map<Task, Integer> taskTopLevelMap = new HashMap<>();
    Queue<Task> beginnableTasks = new PriorityQueue<>((a,b) -> taskBottomLevelMap.get(b) + taskTopLevelMap.get(b) - taskBottomLevelMap.get(a) - taskTopLevelMap.get(a));
    Schedule feasibleSchedule;

    public void calculateTaskStaticAndBottomLevels(List<Task> tasks) {
        for (Task task : tasks) {
            if (task.getOutgoingEdges().size() == 0) {
                taskBottomLevelMap.put(task, task.getWeight());
                taskStaticLevelMap.put(task, task.getWeight());
                Queue<Task> taskQueue = new LinkedList<>();
                taskQueue.add(task);
                while (taskQueue.size() > 0) {
                    Task t = taskQueue.poll();
                    for (Edge e : t.getIngoingEdges()) {
                        Task neighbour = e.getTail();
                        taskBottomLevelMap.compute(neighbour, (k,v) -> {
                            if (v==null) {
                                v=0;
                            }
                            return Math.max(v, taskBottomLevelMap.get(t)+neighbour.getWeight()+e.getWeight());
                        });
                        taskStaticLevelMap.compute(neighbour, (k, v) -> {
                            if (v==null) {
                                v=0;
                            }
                            return Math.max(v, taskStaticLevelMap.get(t)+neighbour.getWeight());
                        });
                        taskQueue.add(neighbour);
                    }
                }
            }
        }
    }

    public void calculateTaskTopLevels(List<Task> tasks) {
        for (Task task : tasks) {
            if (task.getIngoingEdges().size() == 0) {
                taskTopLevelMap.put(task, 0);
                Queue<Task> taskQueue = new LinkedList<>();
                taskQueue.add(task);
                while (taskQueue.size() > 0) {
                    Task t = taskQueue.poll();
                    for (Edge e : t.getOutgoingEdges()) {
                        Task neighbour = e.getHead();
                        taskTopLevelMap.compute(neighbour, (k,v) -> {
                            if (v==null) {
                                v=0;
                            }
                            return Math.max(v, taskTopLevelMap.get(t)+t.getWeight()+e.getWeight());
                        });
                        taskQueue.add(neighbour);
                    }
                }
            }
        }
    }

    public void preprocess(List<Task> tasks) {
        calculateTaskStaticAndBottomLevels(tasks);
        calculateTaskTopLevels(tasks);

        for (Task task : tasks) {
            if (task.getIngoingEdges().size() != 0) {
                taskRequirementsMap.put(task, task.getIngoingEdges().size());
            } else {
                beginnableTasks.add(task);
            }
        }
    }

    public Schedule findFeasibleSchedule(List<Task> tasks, int numProcessors) {
        Schedule schedule = new Schedule(numProcessors, new HashMap<>(taskRequirementsMap), new PriorityQueue<>(beginnableTasks));
        Queue<Task> tasksToSchedule = schedule.getBeginnableTasks();
        while (tasksToSchedule.size()>0) {
            Task t = tasksToSchedule.poll();
            int earliestStartTime = Integer.MAX_VALUE;
            int earliestStartProcessor = -1;
            for (int i = 0; i < numProcessors; i++) {
                int start = getEarliestTimeToSchedule(schedule,t,i);
                if (start < earliestStartTime) {
                    earliestStartTime = start;
                    earliestStartProcessor = i;
                }
            }
            schedule.addTask(t, earliestStartProcessor, earliestStartTime);
            schedule.setEstimatedFinishTime(Math.max(schedule.getEstimatedFinishTime(), earliestStartTime+taskStaticLevelMap.get(t)));
        }
        feasibleSchedule = schedule;
        return feasibleSchedule;
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
                Schedule s = new Schedule(numProcessors, new HashMap<>(taskRequirementsMap), new PriorityQueue<>(beginnableTasks));
                s.addTask(t, 0, 0);
                s.setEstimatedFinishTime(taskStaticLevelMap.get(t));
                scheduleQueue.add(s);
                beginnableTasks.add(t);
            }
        }
        return scheduleQueue;
    }

    public Schedule findOptimalSchedule(List<Task> tasks, int numProcessors) {
        preprocess(tasks);
        findFeasibleSchedule(tasks,numProcessors);

        Queue<Schedule> scheduleQueue = generateInitialSchedules(tasks, numProcessors);
        while (scheduleQueue.size() != 0) {
            Schedule s = scheduleQueue.poll();

            //Check if the schedule is complete
            if (s.getNumberOfTasks() == tasks.size()) {
                return s;
            } else {
                //Get a list of tasks that haven't been allocated to the current schedule 's'
                Queue<Task> tasksToSchedule = s.getBeginnableTasks();

                //Expanding the schedule s, and insert them into the scheduleQueue
                while (tasksToSchedule.size()>0) {
                    Task t = tasksToSchedule.poll();
                    int minDistanceToEnd = taskStaticLevelMap.get(t);
                    for (int i = 0; i < numProcessors; i++) {
                        Schedule newSchedule = s.clone();
                        //Compute the earliest time to schedule the task ('t') on this processor (processor 'i')
                        int earliestStartTime = getEarliestTimeToSchedule(newSchedule, t, i);
                        newSchedule.addTask(t, i, earliestStartTime);
                        newSchedule.setEstimatedFinishTime(Math.max(newSchedule.getEstimatedFinishTime(), earliestStartTime+minDistanceToEnd));
                        if (newSchedule.getEstimatedFinishTime()<feasibleSchedule.getEstimatedFinishTime()) {
                            scheduleQueue.add(newSchedule);
                        }
                    }
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
        Map<Task,Integer> taskProcessorMap = schedule.getTaskProcessorMap();
        for (Edge e : task.getIngoingEdges()) {
            int finishTime = schedule.getTaskFinishTime(e.getTail());
            if (taskProcessorMap.get(e.getTail()) == processor) {
                earliestStartTime = Math.max(earliestStartTime, finishTime);
            } else {
                earliestStartTime = Math.max(earliestStartTime, finishTime + e.getWeight());
            }
        }
        return earliestStartTime;
    }
}
