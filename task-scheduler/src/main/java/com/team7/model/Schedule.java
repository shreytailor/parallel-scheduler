package com.team7.model;

import java.util.*;

public class Schedule {
    private byte[] taskProcessorMap;
    private int[] taskStartTimeMap;
    private byte[] taskRequirementsMap;
    private Queue<Task> beginnableTasks;
    private int estimatedFinishTime = 0;
    private byte tasksCompleted = 0;
    private int[] processorFinishTimes;

    public Schedule(int numTasks, int numProcessors, byte[] taskRequirementsMap, Queue<Task> beginnableTasks) {
        taskProcessorMap = new byte[numTasks];
        taskStartTimeMap = new int[numTasks];
        this.taskRequirementsMap = taskRequirementsMap;
        this.beginnableTasks = beginnableTasks;
        processorFinishTimes = new int[numProcessors];
    }

    public Schedule(byte[] taskProcessorMap, int[] taskStartTimeMap, byte[] taskRequirementsMap, Queue<Task> beginnableTasks, int[] processorFinishTimes, int estimatedFinishTime, byte tasksCompleted) {
        this.taskProcessorMap = taskProcessorMap;
        this.taskStartTimeMap = taskStartTimeMap;
        this.taskRequirementsMap = taskRequirementsMap;
        this.beginnableTasks = beginnableTasks;
        this.processorFinishTimes = processorFinishTimes;
        this.estimatedFinishTime = estimatedFinishTime;
        this.tasksCompleted = tasksCompleted;
    }

    public void addTask(Task n, int processor, int startTime) {
        taskProcessorMap[n.getUniqueID()] = (byte) processor;
        taskStartTimeMap[n.getUniqueID()] = startTime;

        //Checking whether there are any new tasks that we can begin
        for (Edge out : n.getOutgoingEdges()) {
            taskRequirementsMap[out.getHead().getUniqueID()]--;
            if (taskRequirementsMap[out.getHead().getUniqueID()] == 0) {
                beginnableTasks.add(out.getHead());
            }
        }

        processorFinishTimes[processor] = startTime + n.getWeight();
        tasksCompleted++;
    }

    public byte[] getTaskProcessorMap() {
        return taskProcessorMap;
    }

    public int[] getTaskStartTimeMap() {
        return taskStartTimeMap;
    }

    public Queue<Task> getBeginnableTasks() {
        return beginnableTasks;
    }

    public int getNumberOfTasks() {
        return tasksCompleted;
    }

    public int getTaskStartTime(Task n) {
        return taskStartTimeMap[n.getUniqueID()];
    }

    public int getTaskFinishTime(Task n) {
        return taskStartTimeMap[n.getUniqueID()] + n.getWeight();
    }

    public int getTaskProcessor(Task n) {
        return taskProcessorMap[n.getUniqueID()];
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
        return new Schedule(taskProcessorMap.clone(), taskStartTimeMap.clone(), taskRequirementsMap.clone(), new PriorityQueue<>(beginnableTasks), processorFinishTimes.clone(), estimatedFinishTime, tasksCompleted);
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "\n\ttaskProcessorMap=\n" + Arrays.toString(taskProcessorMap) +
                "\n\ttaskStartTimeMap=\n" + Arrays.toString(taskStartTimeMap) +
                "\n\tprocessorFinishTimes=" + Arrays.toString(processorFinishTimes) +
                "\n\tfinishTime=" + estimatedFinishTime +
                "\n\n}";
    }
}
