package com.team7.model;

import java.util.Arrays;
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
        this.finishTime = finishTime;
    }

    public void addTask(Task n, int processor, int startTime, int finishTime) {
        taskProcessorMap.put(n, processor);
        taskStartTimeMap.put(n, startTime);
        taskFinishTimeMap.put(n, finishTime);
        if (processorFinishTimes[processor] > startTime) {
            throw new RuntimeException("Something went wrong");
        }
        processorFinishTimes[processor] = finishTime;
        this.finishTime = Math.max(this.finishTime, finishTime);
    }

    public Map<Task, Integer> getTaskProcessorMap() {
        return taskProcessorMap;
    }

    public Map<Task, Integer> getTaskStartTimeMap() {
        return taskStartTimeMap;
    }

    public Map<Task, Integer> getTaskFinishTimeMap() {return taskFinishTimeMap; }

    public int getNumberOfTasks() {
        return taskProcessorMap.size();
    }

    public int getTaskStartTime(Task n) {
        return taskStartTimeMap.get(n);
    }

    public int getTaskFinishTime(Task n) {
        return taskFinishTimeMap.get(n);
    }

    public int getTaskProcessor(Task n) {
        return taskProcessorMap.get(n);
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

    private <T, S> String mapToString(Map<T, S> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<T, S> e : map.entrySet()) {
            sb.append("\t\t" + e.getKey() + " : " + e.getValue() + "\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "\n\ttaskProcessorMap=\n" + mapToString(taskProcessorMap) +
                "\n\ttaskStartTimeMap=\n" + mapToString(taskStartTimeMap) +
                "\n\ttaskFinishTimeMap=\n" + mapToString(taskFinishTimeMap) +
                "\n\tprocessorFinishTimes=" + Arrays.toString(processorFinishTimes) +
                "\n\tfinishTime=" + finishTime +
                "\n\n}";
    }
}
