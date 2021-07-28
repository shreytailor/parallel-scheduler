package com.team7;

import com.team7.model.Edge;
import com.team7.model.Schedule;
import com.team7.model.Node;

import java.util.*;

public class Scheduler {

    public Schedule AStar(List<Node> nodes, int numProcessors) {
        //note: using a regular queue for now since we are doing brute force
        Queue<Schedule> scheduleQueue = new LinkedList<>();

        //Creating schedules for all the tasks that can be completed at the beginning (i.e. tasks which have no prerequisites)
        for (Node n : nodes) {
            if (n.getIngoingEdges().size() == 0) {
                for (int i = 0; i < numProcessors; i++) {
                    Schedule s = new Schedule(numProcessors);
                    s.addTask(n, i, 0, n.getWeight());
                    scheduleQueue.add(s);
                }
            }
        }

        Schedule optimalSchedule = null;
        while (scheduleQueue.size() != 0) {
            Schedule s = scheduleQueue.poll();

            //Check if the schedule is complete
            if (s.getNumberOfTasks() == nodes.size()) {
                if (optimalSchedule == null || s.getOverallFinishTime() < optimalSchedule.getOverallFinishTime()) {
                    optimalSchedule = s;
                }
            } else {
                Map<Node, Integer> finishedTasks = s.getTaskProcessorMap();
                List<Node> canBegin = new ArrayList<>();

                //Checking which tasks can be started
                for (Node n : nodes) {
                    if (!finishedTasks.containsKey(n)) {
                        boolean able = true;
                        for (Edge required : n.getIngoingEdges()) {
                            if (!finishedTasks.containsKey(required.getTail())) {
                                able = false;
                                break;
                            }
                        }
                        if (able) {
                            canBegin.add(n);
                        }
                    }
                }

                //Creating new schedules for all the new tasks that can be completed
                for (Node n : canBegin) {
                    for (int i = 0; i < numProcessors; i++) {
                        Schedule newSchedule = s.clone();
                        int earliestStartTime = newSchedule.getProcessorFinishTime(i);
                        for (Edge e : n.getIngoingEdges()) {
                            int finishTime = newSchedule.getTaskFinishTime(e.getTail());
                            if (finishedTasks.get(e.getTail()) == i) {
                                earliestStartTime = Math.max(earliestStartTime, finishTime);
                            } else {
                                earliestStartTime = Math.max(earliestStartTime, finishTime + e.getWeight());
                            }
                        }
                        newSchedule.addTask(n, i, earliestStartTime, earliestStartTime + n.getWeight());
                        scheduleQueue.add(newSchedule);
                    }
                }
            }
        }
        return optimalSchedule;
    }
}
