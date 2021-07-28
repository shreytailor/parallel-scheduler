package com.team7.model;

import java.util.HashMap;
import java.util.Map;

public class Schedule {
    Map<Node, Integer> taskProcessorMap;
    Map<Node, Integer> taskStarttimeMap;
    Map<Node, Integer> taskFinishtimeMap;
    int[] processorFinishtimes;
    int finishTime;

    public Schedule(int numProcessors) {
        taskProcessorMap = new HashMap<>();
        taskStarttimeMap = new HashMap<>();
        taskFinishtimeMap = new HashMap<>();
        processorFinishtimes = new int[numProcessors];
        finishTime = 0;
    }

    public Schedule(Map<Node, Integer> taskProcessorMap, Map<Node, Integer> taskStarttimeMap, Map<Node, Integer> taskFinishtimeMap, int[] processorFinishtimes, int finishTime) {
        this.taskProcessorMap = taskProcessorMap;
        this.taskStarttimeMap = taskStarttimeMap;
        this.taskFinishtimeMap = taskFinishtimeMap;
        this.processorFinishtimes = processorFinishtimes;
        this.finishTime=finishTime;
    }

    public void addTask(Node n, int processor, int startTime, int finishTime) {
        taskProcessorMap.put(n, processor);
        taskStarttimeMap.put(n, startTime);
        taskFinishtimeMap.put(n, finishTime);
        if (processorFinishtimes[processor]>startTime) {
            throw new RuntimeException("Something went wrong");
        }
        processorFinishtimes[processor] = finishTime;
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

    public int getProcessorFinishTime(int processor) {
        return processorFinishtimes[processor];
    }

    public int getOverallFinishTime() {
        return finishTime;
    }

    public Schedule clone() {
        return new Schedule(new HashMap<>(taskProcessorMap), new HashMap<>(taskStarttimeMap), new HashMap<>(taskFinishtimeMap), processorFinishtimes.clone(), finishTime);
    }
}
