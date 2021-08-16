package com.team7.model;

import java.util.*;

public class Schedule {
    private byte[] taskProcessorMap;
    private int[] taskStartTimeMap;
    private byte[] taskRequirementsMap;
    private int estimatedFinishTime = 0;
    private byte tasksCompleted = 0;
    private int[] processorFinishTimes;
    private int idleTime;
    private byte partialExpansionIndex = -1;
    private byte normalisationIndex;

    public Schedule(int numTasks, int numProcessors, byte[] taskRequirementsMap) {
        taskProcessorMap = new byte[numTasks];
        Arrays.fill(taskProcessorMap, (byte) -1);
        taskStartTimeMap = new int[numTasks];
        Arrays.fill(taskStartTimeMap, -1);
        this.taskRequirementsMap = taskRequirementsMap;
        processorFinishTimes = new int[numProcessors];
        idleTime = 0;
        normalisationIndex = -1;
    }

    public Schedule(byte[] taskProcessorMap, int[] taskStartTimeMap, byte[] taskRequirementsMap, int[] processorFinishTimes, int estimatedFinishTime, byte tasksCompleted, int idleTime, byte normalisationIndex) {
        this.taskProcessorMap = taskProcessorMap;
        this.taskStartTimeMap = taskStartTimeMap;
        this.taskRequirementsMap = taskRequirementsMap;
        this.processorFinishTimes = processorFinishTimes;
        this.estimatedFinishTime = estimatedFinishTime;
        this.tasksCompleted = tasksCompleted;
        this.idleTime = idleTime;
        this.normalisationIndex = normalisationIndex;
    }

    public void addTask(Task n, int processor, int startTime) {
        if (startTime == 0) {
            normalisationIndex = (byte) n.getUniqueID();
        }
        taskProcessorMap[n.getUniqueID()] = (byte) processor;
        taskStartTimeMap[n.getUniqueID()] = startTime;
        idleTime += startTime - processorFinishTimes[processor];
        taskRequirementsMap[n.getUniqueID()]--;

        //Checking whether there are any new tasks that we can begin
        for (Edge out : n.getOutgoingEdges()) {
            taskRequirementsMap[out.getHead().getUniqueID()]--;
        }

        processorFinishTimes[processor] = startTime + n.getWeight();
        tasksCompleted++;
    }

    public Queue<Integer> getBeginnableTasks() {
        Queue<Integer> beginnableTasks = new LinkedList<>();
        for (int i = partialExpansionIndex + 1; i < taskRequirementsMap.length; i++) {
            if (taskRequirementsMap[i] == 0) {
                beginnableTasks.add(i);
            }
        }
        return beginnableTasks;
    }

    public int[] getTaskStartTimeMap() {
        return taskStartTimeMap;
    }

    public byte[] getTaskProcessorMap() {
        return taskProcessorMap;
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

    public void setPartialExpansionIndex(byte n) {
        partialExpansionIndex = n;
    }

    public byte getNormalisationIndex() {
        return normalisationIndex;
    }

    @Override
    public Schedule clone() {
        return new Schedule(taskProcessorMap.clone(), taskStartTimeMap.clone(), taskRequirementsMap.clone(), processorFinishTimes.clone(), estimatedFinishTime, tasksCompleted, idleTime, normalisationIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Schedule other = (Schedule) o;
        if (this.getEstimatedFinishTime() != other.getEstimatedFinishTime() || this.getNumberOfTasks() != other.getNumberOfTasks()) {
            return false;
        }
        return Arrays.equals(taskProcessorMap,other.taskProcessorMap) && Arrays.equals(taskStartTimeMap ,other.taskStartTimeMap);
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
