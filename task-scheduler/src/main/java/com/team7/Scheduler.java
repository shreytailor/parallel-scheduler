package com.team7;

import com.team7.model.Edge;
import com.team7.model.Schedule;
import com.team7.model.Node;

import java.util.*;

public class Scheduler {

    public Schedule AStar(List<Node> nodes, int numProcessors) {
        Queue<Schedule> scheduleQueue = new LinkedList<>();
        for (Node n : nodes) {
            if (n.getIngoingEdges().size() == 0) {
                for (int i = 0; i < numProcessors; i++) {
                    Schedule s = new Schedule();
                    s.addTask(n, i, 0 , n.getWeight());
                    scheduleQueue.add(s);
                }
            }
        }
        Schedule optimalSchedule = null;
        while (scheduleQueue.size() != 0) {
            Schedule s = scheduleQueue.poll();
            if (s.getNumberOfTasks() == nodes.size()) {
                if (optimalSchedule == null || s.getOverallFinishTime() < optimalSchedule.getOverallFinishTime()) {
                    optimalSchedule = s;
                }
            } else {
                Map<Node, Integer> finishedTasks = s.getTaskProcessorMap();
                List<Node> canComplete = new ArrayList<>();
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
                            canComplete.add(n);
                        }
                    }
                }
                for (Node n : canComplete) {
                    for (int i=0;i<numProcessors;i++) {
                        Schedule newSchedule = s.clone();
                        int earliestStartTime=0;
                        for (Edge e : n.getIngoingEdges()) {
                            int finishTime = newSchedule.getTaskFinishTime(e.getTail());
                            if (finishedTasks.get(e.getTail())==i) {
                                earliestStartTime = Math.max(earliestStartTime,finishTime);
                            } else {
                                earliestStartTime = Math.max(earliestStartTime,finishTime+e.getTail().getWeight());
                            }
                        }
                        newSchedule.addTask(n,i,earliestStartTime,earliestStartTime+n.getWeight());
                        scheduleQueue.add(newSchedule);
                    }
                }
            }
        }
        return optimalSchedule;
    }
}
