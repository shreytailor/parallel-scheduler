package com.team7.model;

import java.util.HashMap;
import java.util.Map;

public class Schedule {
    Map<Task, Integer> taskProcessorMap;
    Map<Task, Integer> taskStartTimeMap;
    Map<Task, Integer> taskFinishTimeMap;
    int[] processorFinishTimes;
    int finishTime;

    public Schedule(int numProcessors) {
        taskProcessorMap = new HashMap<>();
        taskStartTimeMap = new HashMap<>();
        taskFinishTimeMap = new HashMap<>();
        processorFinishTimes = new int[numProcessors];
        finishTime = 0;
    }

    public Schedule(Map<Task, Integer> taskProcessorMap, Map<Task, Integer> taskStartTimeMap, Map<Task, Integer> taskFinishTimeMap, int[] processorFinishTimes, int finishTime) {
        this.taskProcessorMap = taskProcessorMap;
        this.taskStartTimeMap = taskStartTimeMap;
        this.taskFinishTimeMap = taskFinishTimeMap;
        this.processorFinishTimes = processorFinishTimes;
        this.finishTime=finishTime;
    }

    public void addTask(Task n, int processor, int startTime, int finishTime) {
        taskProcessorMap.put(n, processor);
        taskStartTimeMap.put(n, startTime);
        taskFinishTimeMap.put(n, finishTime);
        if (processorFinishTimes[processor]>startTime) {
            throw new RuntimeException("Something went wrong");
        }
        processorFinishTimes[processor] = finishTime;
        this.finishTime = Math.max(this.finishTime,finishTime);
    }

    public Map<Task, Integer> getTaskProcessorMap() {
        return taskProcessorMap;
    }

    public int getNumberOfTasks() {
        return taskProcessorMap.size();
    }

    public int getTaskFinishTime(Task n) {
        return taskFinishTimeMap.get(n);
    }

    public int getProcessorFinishTime(int processor) {
        return processorFinishTimes[processor];
    }

    public int getOverallFinishTime() {
        return finishTime;
    }

    public Schedule clone() {
        return new Schedule(new HashMap<>(taskProcessorMap), new HashMap<>(taskStartTimeMap), new HashMap<>(taskFinishTimeMap), processorFinishTimes.clone(), finishTime);
    }
}
