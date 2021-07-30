package com.team7;

import com.team7.model.Edge;
import com.team7.model.Schedule;
import com.team7.model.Task;

import java.util.*;

public class Scheduler {

    public Schedule AStar(List<Task> tasks, int numProcessors) {
        //note: using a regular queue for now since we are doing brute force
        Queue<Schedule> scheduleQueue = generateOneTaskSchedules(tasks, numProcessors);

        Schedule optimalSchedule = null;
        while (scheduleQueue.size() != 0) {
            Schedule s = scheduleQueue.poll();

            //Check if the schedule is complete
            if (s.getNumberOfTasks() == tasks.size()) {
                //If schedule is complete, check it is more optimal than the previous 'optimalSchedule'
                if (isMoreOptimal(optimalSchedule, s)) {
                    optimalSchedule = s;
                }
            } else {
                Map<Task, Integer> taskProcessorMap = s.getTaskProcessorMap();
                //Get a list of tasks that haven't been allocated to the current schedule 's'
                List<Task> tasksToSchedule = getTasksToSchedule(tasks, taskProcessorMap);

                //Expanding the schedule s, and insert them into the scheduleQueue
                for (Task t : tasksToSchedule) {
                    for (int i = 0; i < numProcessors; i++) {
                        Schedule newSchedule = s.clone();
                        //Compute earliest time to schedule the task ('t') on this processor (processor 'i')
                        int earliestStartTime = getEarliestTimeToSchedule(taskProcessorMap, t, i, newSchedule);
                        newSchedule.addTask(t, i, earliestStartTime, earliestStartTime + t.getWeight());
                        scheduleQueue.add(newSchedule);
                    }
                }
            }
        }
        return optimalSchedule;
    }

    /**
     * Generates a list of schedules (formally, States), with one task scheduled that has no prerequisites.
     *
     * @param tasks
     * @param numProcessors
     * @return
     */
    private LinkedList<Schedule> generateOneTaskSchedules(List<Task> tasks, int numProcessors) {
        LinkedList<Schedule> scheduleQueue = new LinkedList<>();

        //Creating schedules for all the tasks that can be completed at the beginning (i.e. tasks which have no prerequisites)
        for (Task t : tasks) {
            if (t.getIngoingEdges().size() == 0) {
                for (int i = 0; i < numProcessors; i++) {
                    Schedule s = new Schedule(numProcessors);
                    s.addTask(t, i, 0, t.getWeight());
                    scheduleQueue.add(s);
                }
            }
        }
        return scheduleQueue;
    }

    /**
     * Returns a boolean indicating that the current schedule that is complete
     * is a better solution than previous 'optimalSchedule'.
     * That is, return true if finish time of the current schedule is lower than the current optimalSchedule.
     *
     * @param optimalSchedule
     * @param s
     * @return
     */
    private boolean isMoreOptimal(Schedule optimalSchedule, Schedule s) {
        return optimalSchedule == null || s.getOverallFinishTime() < optimalSchedule.getOverallFinishTime();
    }

    /**
     * On the given processor, find the earliest time we can schedule the given task
     *
     * @param taskProcessorMap
     * @param t
     * @param i
     * @param newSchedule
     * @return
     */
    private int getEarliestTimeToSchedule(Map<Task, Integer> taskProcessorMap, Task t, int i, Schedule newSchedule) {
        int earliestStartTime = newSchedule.getProcessorFinishTime(i);
        for (Edge e : t.getIngoingEdges()) {
            int finishTime = newSchedule.getTaskFinishTime(e.getTail());
            if (taskProcessorMap.get(e.getTail()) == i) {
                earliestStartTime = Math.max(earliestStartTime, finishTime);
            } else {
                earliestStartTime = Math.max(earliestStartTime, finishTime + e.getWeight());
            }
        }
        return earliestStartTime;
    }


    /**
     * Returns the list of tasks that we can schedule
     *
     * @param tasks            The grand list of tasks to be scheduled
     * @param taskProcessorMap Map that maps currently allocated tasks of the schedule to processors
     * @return
     */
    private List<Task> getTasksToSchedule(List<Task> tasks, Map<Task, Integer> taskProcessorMap) {
        List<Task> canBegin = new ArrayList<>();

        //Checking which tasks can be started
        for (Task t : tasks) {
            if (!taskProcessorMap.containsKey(t)) {
                boolean able = true;
                for (Edge required : t.getIngoingEdges()) {
                    if (!taskProcessorMap.containsKey(required.getTail())) {
                        able = false;
                        break;
                    }
                }
                if (able) {
                    canBegin.add(t);
                }
            }
        }
        return canBegin;
    }


}
