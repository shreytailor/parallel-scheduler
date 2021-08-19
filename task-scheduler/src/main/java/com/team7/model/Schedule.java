package com.team7.model;

import sun.awt.image.ImageWatched;

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
    private byte[] processorFirstTask;

    public Schedule(int numTasks, int numProcessors, byte[] taskRequirementsMap) {
        taskProcessorMap = new byte[numTasks];
        Arrays.fill(taskProcessorMap, (byte) -1);
        taskStartTimeMap = new int[numTasks];
        Arrays.fill(taskStartTimeMap, -1);
        this.taskRequirementsMap = taskRequirementsMap;
        processorFinishTimes = new int[numProcessors];
        idleTime = 0;
        normalisationIndex = -1;
        processorFirstTask = new byte[numProcessors];
        Arrays.fill(processorFirstTask, (byte) -1);
    }

    public Schedule(byte[] taskProcessorMap, int[] taskStartTimeMap,
                    byte[] taskRequirementsMap, int[] processorFinishTimes,
                    int estimatedFinishTime, byte tasksCompleted,
                    int idleTime, byte normalisationIndex, byte[] processorFirstTask) {
        this.taskProcessorMap = taskProcessorMap;
        this.taskStartTimeMap = taskStartTimeMap;
        this.taskRequirementsMap = taskRequirementsMap;
        this.processorFinishTimes = processorFinishTimes;
        this.estimatedFinishTime = estimatedFinishTime;
        this.tasksCompleted = tasksCompleted;
        this.idleTime = idleTime;
        this.normalisationIndex = normalisationIndex;
        this.processorFirstTask = processorFirstTask;
    }

    public void addTask(Task n, int processor, int startTime) {
        if (startTime == 0) {
            normalisationIndex = n.getUniqueID();
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
        if (processorFirstTask[processor]==-1) {
            processorFirstTask[processor] = n.getUniqueID();
        }
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

    public int[] getProcessorFinishTimes() {
        return processorFinishTimes;
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
        return new Schedule(taskProcessorMap.clone(), taskStartTimeMap.clone(),
                taskRequirementsMap.clone(), processorFinishTimes.clone(),
                estimatedFinishTime, tasksCompleted,
                idleTime, normalisationIndex, processorFirstTask);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Schedule other = (Schedule) o;
        if (this.getEstimatedFinishTime() != other.getEstimatedFinishTime() || this.getNumberOfTasks() != other.getNumberOfTasks()) {
            return false;
        }

        Map<Byte, List> processorTasks = new HashMap<>();
        for (int i=0;i<processorFirstTask.length;i++) {
            if (processorFirstTask[i] != -1) {
                List<Byte> l = new ArrayList<>();
                l.add(processorFirstTask[i]);
                for (byte j=0;j<taskProcessorMap.length;j++) {
                    if (taskProcessorMap[j] == i) {
                        l.add(j);
                    }
                }
                l.sort(Comparator.comparingInt(a -> taskStartTimeMap[a]));
                processorTasks.put(processorFirstTask[i], l);
            }
        }

        Map<Byte, List> otherProcessorTasks = new HashMap<>();
        for (int i=0;i<other.processorFirstTask.length;i++) {
            if (other.processorFirstTask[i] != -1) {
                List<Byte> l = new ArrayList<>();
                l.add(other.processorFirstTask[i]);
                for (byte j=0;j<other.taskProcessorMap.length;j++) {
                    if (other.taskProcessorMap[j] == i) {
                        l.add(j);
                    }
                }
                l.sort(Comparator.comparingInt(a -> other.taskStartTimeMap[a]));
                otherProcessorTasks.put(other.processorFirstTask[i], l);
            }
        }

        for (Byte key : processorTasks.keySet()) {
            List<Byte> l1 = processorTasks.get(key);
            List<Byte> l2 = otherProcessorTasks.get(key);
            if (!l1.equals(l2)) {
                return false;
            }
            for (Byte b : l1) {
                if (taskStartTimeMap[b] != other.taskStartTimeMap[b]) {
                    return false;
                }
            }
        }
        return true;
//        return Arrays.equals(taskProcessorMap,other.taskProcessorMap) && Arrays.equals(taskStartTimeMap ,other.taskStartTimeMap);
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
