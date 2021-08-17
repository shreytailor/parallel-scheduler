package com.team7.algorithm;

import com.team7.model.Graph;
import com.team7.model.Schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class ParallelSchedulerShareEachLoop extends Scheduler{
    private int numThreads;


    public ParallelSchedulerShareEachLoop(Graph g, int numOfProcessors, int numThreads) {
        super(g, numOfProcessors);
        this.numThreads = numThreads;
    }


    public ParallelSchedulerShareEachLoop(Graph g, int numOfProcessors) {
        super(g, numOfProcessors);
        this.numThreads = 4;
    }

    private class ExpansionWorker implements Callable<Schedule> {
        private Schedule s;

        public ExpansionWorker(Schedule s){
            this.s = s;
        }
        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public Schedule call() throws Exception {
            if (s == null || s.getNumberOfTasks() == tasks.length) {
                return s;
            }
            expandSchedule(s);
            return null;
        }
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
        ExpansionWorker[] workers = new ExpansionWorker[numThreads];


        while (scheduleQueue.size() != 0) {
            for (int i = 0; i < numThreads; i++) {
                workers[i] = new ExpansionWorker(scheduleQueue.poll());
            }

            List<Future<Schedule>> results = null;
            try {
                results = executor.invokeAll(Arrays.asList(workers));
                Schedule bestSchedule = null;
                for (Future<Schedule> result : results) {
                    Schedule schedule = result.get();
                    if(schedule != null){
                        if (bestSchedule==null) {
                            bestSchedule=schedule;
                        } else if (schedule.getEstimatedFinishTime()<bestSchedule.getEstimatedFinishTime()){
                            bestSchedule=schedule;
                        }
                    }
                }
                if (bestSchedule!=null) {
                    return bestSchedule;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }
        executor.shutdown();

        return feasibleSchedule;
    }

}
