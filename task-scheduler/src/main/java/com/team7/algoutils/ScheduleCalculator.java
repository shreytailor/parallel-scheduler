package com.team7.algoutils;

import com.team7.model.Edge;
import com.team7.model.Schedule;
import com.team7.model.Task;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class ScheduleCalculator {

    /**
     * Generates a list of schedules (formally, States), with one task scheduled that has no prerequisites.
     * @param tasks
     * @param processors
     * @param taskRequirementsMap
     * @param beginnableTasks
     * @param taskStaticLevelMap
     * @param length
     */
    public static Queue<Schedule> generateInitialSchedules(Task[] tasks, int processors, byte[] taskRequirementsMap, Queue<Task> beginnableTasks, int[] taskStaticLevelMap, int length) {
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
     * @return
     */
    public static int getEarliestTimeToSchedule(Schedule schedule, Task task, int processor) {
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

    public static Schedule calculateAttributesForNewSchedule(Schedule s, Task t, int minDistanceToEnd, int i) {
        Schedule newSchedule = s.clone();
        //Compute the earliest time to schedule the task ('t') on this processor (processor 'i')
        int earliestStartTime = getEarliestTimeToSchedule(newSchedule, t, i);
        newSchedule.addTask(t, i, earliestStartTime);
        newSchedule.setEstimatedFinishTime(Math.max(newSchedule.getEstimatedFinishTime(), earliestStartTime + minDistanceToEnd));
        return newSchedule;
    }


}
