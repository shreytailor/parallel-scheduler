package com.team7.model;

import java.util.*;

public class Schedule {
    Map<Task, Integer> taskProcessorMap = new HashMap<>();
    Map<Task, Integer> taskStartTimeMap= new HashMap<>();
    Map<Task, Integer> taskRequirementsMap;
    Set<Task> beginnableTasks;
    int estimatedFinishTime=0;
    int[] processorFinishTimes;

    public Schedule(int numProcessors, Map<Task,Integer> taskRequirementsMap, Set<Task> beginnableTasks) {
        processorFinishTimes = new int[numProcessors];
        this.taskRequirementsMap=taskRequirementsMap;
        this.beginnableTasks=beginnableTasks;
    }

    public Schedule(Map<Task, Integer> taskProcessorMap, Map<Task, Integer> taskStartTimeMap, Map<Task,Integer> taskRequirementsMap, Set<Task> beginnableTasks, int[] processorFinishTimes, int estimatedFinishTime) {
        this.taskProcessorMap = taskProcessorMap;
        this.taskStartTimeMap = taskStartTimeMap;
        this.taskRequirementsMap = taskRequirementsMap;
        this.beginnableTasks = beginnableTasks;
        this.processorFinishTimes = processorFinishTimes;
        this.estimatedFinishTime = estimatedFinishTime;
    }

    public void addTask(Task n, int processor, int startTime) {
        taskProcessorMap.put(n, processor);
        taskStartTimeMap.put(n, startTime);
        for (Edge out : n.getOutgoingEdges()) {
            taskRequirementsMap.compute(out.getHead(), (k,v)-> {
                v--;
                if (v==0) {
                    beginnableTasks.add(out.getHead());
                }
                return v;
            });
        }
        if (processorFinishTimes[processor] > startTime) {
            throw new RuntimeException("Something went wrong");
        }
        processorFinishTimes[processor] = startTime + n.getWeight();
    }

    public Map<Task, Integer> getTaskProcessorMap() {
        return taskProcessorMap;
    }

    public Map<Task, Integer> getTaskStartTimeMap() {
        return taskStartTimeMap;
    }

    public Set<Task> getBeginnableTasks() {
        return beginnableTasks;
    }

    public int getNumberOfTasks() {
        return taskProcessorMap.size();
    }

    public int getTaskStartTime(Task n) {
        return taskStartTimeMap.get(n);
    }

    public int getTaskFinishTime(Task n) {
        return taskStartTimeMap.get(n)+n.getWeight();
    }

    public int getTaskProcessor(Task n) {
        return taskProcessorMap.get(n);
    }

    public int getProcessorFinishTime(int processor) {
        return processorFinishTimes[processor];
    }

    public void setEstimatedFinishTime(int n) {
        estimatedFinishTime = n;
    }

    public int getEstimatedFinishTime() {
        return estimatedFinishTime;
    }

    @Override
    public Schedule clone() {
        return new Schedule(new HashMap<>(taskProcessorMap), new HashMap<>(taskStartTimeMap), new HashMap<>(taskRequirementsMap), new HashSet<>(beginnableTasks), processorFinishTimes.clone(), estimatedFinishTime);
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
                "\n\tprocessorFinishTimes=" + Arrays.toString(processorFinishTimes) +
                "\n\tfinishTime=" + estimatedFinishTime +
                "\n\n}";
    }
}
