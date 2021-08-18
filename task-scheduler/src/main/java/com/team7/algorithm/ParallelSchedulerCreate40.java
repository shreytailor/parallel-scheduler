package com.team7.algorithm;

import com.team7.model.Graph;
import com.team7.model.Schedule;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

public class ParallelSchedulerCreate40 extends Scheduler{
    private int numThreads;


    public ParallelSchedulerCreate40(Graph g, int numOfProcessors, int numThreads) {
        super(g, numOfProcessors);
        this.numThreads = numThreads;
    }


    public ParallelSchedulerCreate40(Graph g, int numOfProcessors) {
        super(g, numOfProcessors);
        this.numThreads = 4;
    }

    private class IndependentWorker implements Callable<Schedule> {
        private Scheduler scheduler;

        public IndependentWorker(Scheduler scheduler){
            this.scheduler = scheduler;
        }

        @Override
        public Schedule call() throws Exception {
            scheduler.findFeasibleSchedule();

            // (1) OPEN priority queue, sorted by f
            scheduler.generateInitialSchedules();
            while (scheduler.scheduleQueue.size() != 0) {
                // (2) Remove from OPEN the search state s with the smallest f
                Schedule s = scheduler.scheduleQueue.poll();

                // (3) If s is the goal state, a complete and optimal schedule is found and the algorithm stops;
                // otherwise, go to the next step.
                if (s.getNumberOfTasks() == tasks.length) {
                    return s;
                }
                // (4) Expand the state s, which produces new state s'. Compute f and put s' into OPEN. Go to (2).
                scheduler.expandSchedule(s);
            }

//            return scheduler.feasibleSchedule;
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

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        IndependentWorker[] workers = new IndependentWorker[numThreads];

        // if 4 threads, there will be more than 40 states created here
        while(scheduleQueue.size()<numThreads*10){
            Schedule schedule = scheduleQueue.poll();
            expandSchedule(schedule);
        }

        // clone this scheduler
        Scheduler clonedScheduler = Scheduler.cloneScheduler(this);


        // for all thread worker except 1, distribute 10 tasks each
        for(int i = 0; i<numThreads-1; i++){
            Queue<Schedule> startingWorkForThisThread = Scheduler.createEmptyPriorityScheduleQueue();

            for(int j = 0; j<10; j++) {
                startingWorkForThisThread.add(scheduleQueue.poll());
            }

            clonedScheduler.setScheduleQueue(startingWorkForThisThread);
            workers[i] = new IndependentWorker(clonedScheduler);
        }

        // allocate the rest to the last thread worker
        Queue<Schedule> startingWorkForThisThread = Scheduler.createEmptyPriorityScheduleQueue();
        while(!scheduleQueue.isEmpty()) {
            startingWorkForThisThread.add(scheduleQueue.poll());
        }

        workers[numThreads-1] = new IndependentWorker(clonedScheduler);


        try {
            List<Future<Schedule>> schedules = null;
            schedules = executor.invokeAll(Arrays.asList(workers));

            Schedule bestSchedule = null;
            for (Future<Schedule> wrappedSchedule : schedules) {
                Schedule schedule = wrappedSchedule.get();
                if (bestSchedule == null) {
                    bestSchedule = schedule;
                }else{
                    if (schedule.getEstimatedFinishTime() < bestSchedule.getEstimatedFinishTime()) {
                        bestSchedule = schedule;
                    }
                }
            }

            return bestSchedule;

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


        return feasibleSchedule;
    }


    /**
     * IMPORTANT: access to treeset had to be synchronized, otherwise NPE
     * @param s
     */
    public void expandSchedule(Schedule s) {
        //Expanding the schedule s, and insert them into the scheduleQueue
        for (Integer t : s.getBeginnableTasks()) {
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
        synchronized (Scheduler.class){
            visitedSchedules.add(s);
        }
    }

}
