package com.team7.model;

import java.util.HashMap;
import java.util.Map;

public class Schedule {
    Map<Node, Integer> taskProcessorMap;
    Map<Node, Integer> taskStarttimeMap;
    Map<Node, Integer> taskFinishtimeMap;
    int finishTime;

    public Schedule() {
        taskProcessorMap = new HashMap<>();
        taskStarttimeMap = new HashMap<>();
        taskFinishtimeMap = new HashMap<>();
        finishTime = 0;
    }

    public Schedule(Map<Node, Integer> taskProcessorMap, Map<Node, Integer> taskStarttimeMap, Map<Node, Integer> taskFinishtimeMap, int finishTime) {
        this.taskProcessorMap = taskProcessorMap;
        this.taskStarttimeMap = taskStarttimeMap;
        this.taskFinishtimeMap = taskFinishtimeMap;
        this.finishTime=finishTime;
    }

    public void addTask(Node n, int processor, int startTime, int finishTime) {
        taskProcessorMap.put(n, processor);
        this.finishTime = Math.max(this.finishTime,finishTime);
    }

    public Map<Node, Integer> getTaskProcessorMap() {
        return taskProcessorMap;
    }

    public int getNumberOfTasks() {
        return taskProcessorMap.size();
    }

    public int getTaskFinishTime(Node n) {
        return taskFinishtimeMap.get(n);
    }

    public int getOverallFinishTime() {
        return finishTime;
    }

    public Schedule clone() {
        return new Schedule(new HashMap<>(taskProcessorMap), new HashMap<>(taskStarttimeMap), new HashMap<>(taskFinishtimeMap), finishTime);
    }
}
