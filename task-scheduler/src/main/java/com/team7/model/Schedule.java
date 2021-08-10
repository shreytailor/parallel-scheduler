package com.team7.model;

import java.util.*;

public class Schedule {
    private byte[] taskProcessorMap;
    private int[] taskStartTimeMap;
    private byte[] taskRequirementsMap;
    private int estimatedFinishTime = 0;
    private byte tasksCompleted = 0;
    private int[] processorFinishTimes;
    private int idleTime = 0;
    private byte partialExpansionIndex=-1;

    public Schedule(int numTasks, int numProcessors, byte[] taskRequirementsMap) {
        taskProcessorMap = new byte[numTasks];
        taskStartTimeMap = new int[numTasks];
        this.taskRequirementsMap = taskRequirementsMap;
        processorFinishTimes = new int[numProcessors];
    }

    public Schedule(byte[] taskProcessorMap, int[] taskStartTimeMap, byte[] taskRequirementsMap, int[] processorFinishTimes, int estimatedFinishTime, byte tasksCompleted) {
        this.taskProcessorMap = taskProcessorMap;
        this.taskStartTimeMap = taskStartTimeMap;
        this.taskRequirementsMap = taskRequirementsMap;
        this.processorFinishTimes = processorFinishTimes;
        this.estimatedFinishTime = estimatedFinishTime;
        this.tasksCompleted = tasksCompleted;
    }

    public void addTask(Task n, int processor, int startTime) {
        taskProcessorMap[n.getUniqueID()] = (byte) processor;
        taskStartTimeMap[n.getUniqueID()] = startTime;
        idleTime+=startTime - processorFinishTimes[processor];
        taskRequirementsMap[n.getUniqueID()]--;

        //Checking whether there are any new tasks that we can begin
        for (Edge out : n.getOutgoingEdges()) {
            taskRequirementsMap[out.getHead().getUniqueID()]--;
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

    public Queue<Integer> getBeginnableTasks() {
        Queue<Integer> beginnableTasks = new LinkedList<>();
        for (int i = partialExpansionIndex+1; i < taskRequirementsMap.length; i++) {
            if (taskRequirementsMap[i] == 0) {
                beginnableTasks.add(i);
            }
        }
        return beginnableTasks;
    }

    public boolean isBeginnable(Task t) {
        return taskRequirementsMap[t.getUniqueID()] == 0;
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

    public int getIdleTime() {
        return idleTime;
    }

    public int getPartialExpansionIndex() {
        return partialExpansionIndex;
    }

    public void setPartialExpansionIndex(byte n) {
        partialExpansionIndex = n;
    }

    @Override
    public Schedule clone() {
        return new Schedule(taskProcessorMap.clone(), taskStartTimeMap.clone(), taskRequirementsMap.clone(), processorFinishTimes.clone(), estimatedFinishTime, tasksCompleted);
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
