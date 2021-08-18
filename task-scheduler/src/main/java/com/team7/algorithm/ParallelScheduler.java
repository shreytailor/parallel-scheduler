package com.team7.algorithm;

import com.team7.model.Graph;
import com.team7.model.Schedule;
import com.team7.model.Task;

import java.util.*;
import java.util.concurrent.*;

public class ParallelScheduler extends Scheduler {
    ExecutorService executor;
    ExpansionWorker[] workers;
    private int numThreads;


    public ParallelScheduler(Graph g, int numOfProcessors, int numThreads) {
        super(g, numOfProcessors);
        scheduleQueue = new PriorityBlockingQueue<Schedule>(11, (a, b) -> {
            int n = a.getEstimatedFinishTime() - b.getEstimatedFinishTime();
            if (n == 0) {
                return b.getNumberOfTasks() - a.getNumberOfTasks();
            }
            return n;
        });
        this.numThreads = numThreads;
        executor = Executors.newFixedThreadPool(numThreads);
        workers = new ExpansionWorker[numThreads];
    }


    public ParallelScheduler(Graph g, int numOfProcessors) {
        super(g, numOfProcessors);
        scheduleQueue = new PriorityBlockingQueue<Schedule>(11, (a, b) -> {
            int n = a.getEstimatedFinishTime() - b.getEstimatedFinishTime();
            if (n == 0) {
                return b.getNumberOfTasks() - a.getNumberOfTasks();
            }
            return n;
        });
        this.numThreads = 4;
        executor = Executors.newFixedThreadPool(numThreads);
        workers = new ExpansionWorker[numThreads];
    }

    private class ExpansionWorker implements Callable<Schedule> {
        private Schedule s;

        public ExpansionWorker(Schedule s) {
            this.s = s;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         */
        @Override
        public Schedule call() {
            if (s == null || s.getNumberOfTasks() == tasks.length) {
                return s;
            }
            expandSchedule(s);
            return null;
        }
    }

    public synchronized Schedule poll(){
        return scheduleQueue.poll();
    }
    /**
     * Generates an optimal schedule using an A* algorithm.
     *
     * @return an optimal schedule
     */
    public Schedule findOptimalSchedule() {
        findFeasibleSchedule();
        // (1) OPEN priority queue, sorted by f
        generateInitialSchedules();

        while (scheduleQueue.size() != 0) {
            for (int i = 0; i < numThreads; i++) {
                workers[i] = new ExpansionWorker(scheduleQueue.poll());
            }

            List<Future<Schedule>> results;
            try {
                results = executor.invokeAll(Arrays.asList(workers));
                Schedule bestSchedule = null;
                for (Future<Schedule> result : results) {
                    Schedule schedule = result.get();
                    if (schedule != null &&
                            (bestSchedule == null || schedule.getEstimatedFinishTime() < bestSchedule.getEstimatedFinishTime())) {
                        bestSchedule = schedule;
                    }
                }
                if (bestSchedule != null) {
                    return bestSchedule;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return feasibleSchedule;
    }


    /**
     * IMPORTANT: access to treeset had to be synchronized, otherwise NPE
     *
     * @param s
     */
    public void expandSchedule(Schedule s) {
        //Expanding the schedule s, and insert them into the scheduleQueue
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
                    scheduleQueue.add(newSchedule);
                    if (newSchedule.getEstimatedFinishTime() == s.getEstimatedFinishTime()) {
                        s.setPartialExpansionIndex(t.byteValue());
                        scheduleQueue.add(s);
                        return;
                    }
                }
            }
        }
//        synchronized (Scheduler.class) {
        synchronized (this) {
            visitedSchedules.add(s);
        }
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
