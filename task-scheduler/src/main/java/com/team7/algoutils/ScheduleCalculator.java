package com.team7.algoutils;

import com.team7.model.Edge;
import com.team7.model.Schedule;
import com.team7.model.Task;

public class ScheduleCalculator {
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


}
