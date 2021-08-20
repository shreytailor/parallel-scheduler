package com.team7.algorithm;

import com.team7.model.Graph;
import com.team7.model.Schedule;
import com.team7.model.Task;

import java.util.*;
import java.util.concurrent.*;

public class ParallelScheduler extends Scheduler {
    private ExecutorService executorService;
    private IndependentWorker[] workers;
    private int numThreads;
    private Schedule bestSchedule;

    public ParallelScheduler(Graph g, int numOfProcessors, int numThreads) {
        super(g, numOfProcessors);
        executorService = Executors.newFixedThreadPool(numThreads);
        workers = new IndependentWorker[numThreads];
        for (int i = 0; i < numThreads; i++) {
            workers[i] = new IndependentWorker();
        }
        this.numThreads = numThreads;
    }

    public ParallelScheduler(Graph g, int numOfProcessors) {
        this(g, numOfProcessors, 4);
    }

    @Override
    public int getInfoOpenStates(){
        int numberOfOpenStates = 0;
        for (IndependentWorker worker : workers) {
            numberOfOpenStates += worker.getOpenStatesSize();
        }
        return numberOfOpenStates;
    }

    /**
     * Generates an optimal schedule using an A* algorithm.
     *
     * @return an optimal schedule
     */
    public Schedule findOptimalSchedule() {
        bestSchedule = findFeasibleSchedule();

        generateInitialSchedules();

        // if 4 threads, there will be more than 40 states created here
        while (scheduleQueue.size() > 0 && scheduleQueue.size() < numThreads * 10) {
            Schedule schedule = scheduleQueue.poll();
            if (schedule.getNumberOfTasks() == tasks.length) {
                return schedule;
            }
            expandSchedule(schedule);
        }

        // for all thread worker except 1, distribute 10 tasks each
        int index = 0;
        while (scheduleQueue.size() > 0) {
            workers[index % numThreads].addSchedule(scheduleQueue.poll());
            index++;
        }

        try {
            executorService.invokeAll(Arrays.asList(workers));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return bestSchedule;
    }

    public void shutdown() {
        executorService.shutdownNow();
    }

    private class IndependentWorker implements Callable<Schedule> {
        private Queue<Schedule> schedules = createScheduleQueue();

        public void addSchedule(Schedule s) {
            schedules.add(s);
        }

        public int getOpenStatesSize(){
            return schedules.size();
        }

        @Override
        public Schedule call() {
            // (1) OPEN priority queue, sorted by f
            while (schedules.size() != 0) {
                // (2) Remove from OPEN the search state s with the smallest f
                Schedule s = schedules.poll();

                synchronized (this) {
                    if (s.getEstimatedFinishTime() >= bestSchedule.getEstimatedFinishTime()) {
                        break;
                    }
                    // (3) If s is the goal state, a complete and optimal schedule is found and the algorithm stops;
                    // otherwise, go to the next step.
                    if (s.getNumberOfTasks() == tasks.length && s.getEstimatedFinishTime() < bestSchedule.getEstimatedFinishTime()) {
                        bestSchedule = s;
                    }
                }
                // (4) Expand the state s, which produces new state s'. Compute f and put s' into OPEN. Go to (2).
                expandSchedule(s, schedules);
            }
            return null;
        }
    }

    private void expandSchedule(Schedule s, Queue<Schedule> schedules) {
        Set<Task> equivalent = new HashSet<>();
        for (Integer t : s.getBeginnableTasks()) {
            if (equivalent.contains(tasks[t])) {
                continue;
            }
            equivalent.addAll(taskEquivalencesMap[t]);
            boolean normalised = false;
            for (int i = 0; i < processors; i++) {
                int earliestStartTime = calculateEarliestTimeToSchedule(s, tasks[t], i);
                if (earliestStartTime == 0) {
                    if (normalised || t < s.getNormalisationIndex()) {
                        continue;
                    }
                    normalised = true;
                }
                Schedule newSchedule = generateNewSchedule(s, tasks[t], i, earliestStartTime);

                //Only add the new schedule to the queue if it can potentially be better than the feasible schedule.
                if (newSchedule.getEstimatedFinishTime() < feasibleSchedule.getEstimatedFinishTime() &&
                        !containsEquivalentSchedule(newSchedule, tasks[t]) &&
                        !visitedSchedules.contains(newSchedule)) {
                    schedules.add(newSchedule);
                    if (newSchedule.getEstimatedFinishTime() == s.getEstimatedFinishTime()) {
                        s.setPartialExpansionIndex(t.byteValue());
                        schedules.add(s);
                        return;
                    }
                }
            }
        }
        synchronized (this) {
            visitedSchedules.add(s);
        }
    }
}
