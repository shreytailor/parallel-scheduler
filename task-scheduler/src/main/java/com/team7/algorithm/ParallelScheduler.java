package com.team7.algorithm;

import com.team7.Entrypoint;
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

        if (numThreads == 0) {
            throw new RuntimeException("number of threads not specified");
        }
        executorService = Executors.newFixedThreadPool(numThreads);
        workers = new IndependentWorker[numThreads];
        for (int i = 0; i < numThreads; i++) {
            workers[i] = new IndependentWorker();
        }
        this.numThreads = numThreads;
    }

    @Override
    public int getInfoOpenStates() {
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
    @Override
    public Schedule findOptimalSchedule() {
        bestSchedule = findFeasibleSchedule();

        generateInitialSchedules();

        while (openSchedules.size() > 0 && openSchedules.size() < numThreads * 10) {
            Schedule schedule = openSchedules.pollFirst();
            if (schedule.getNumberOfTasks() == tasks.length) {
                Entrypoint.stopTimerLabel();
                sharedState = schedule;
                return schedule;
            }
            expandSchedule(schedule, openSchedules);
        }

        // distribute the tasks
        int index = 0;
        while (openSchedules.size() > 0) {
            workers[index % numThreads].addSchedule(openSchedules.pollFirst());
            index++;
        }

        try {
            executorService.invokeAll(Arrays.asList(workers));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Entrypoint.stopTimerLabel();
        sharedState = bestSchedule;
        return bestSchedule;
    }

    @Override
    protected void addToVisitedSchedules(Schedule s) {
        synchronized (this) {
            visitedSchedules.add(s);
        }
    }

    private class IndependentWorker implements Callable<Schedule> {
        private TreeSet<Schedule> schedules = createScheduleSet();

        public void addSchedule(Schedule s) {
            schedules.add(s);
        }

        public int getOpenStatesSize() {
            return schedules.size();
        }

        @Override
        public Schedule call() {
            // (1) OPEN priority queue, sorted by f
            while (schedules.size() != 0) {
                Schedule s = schedules.pollFirst();

                synchronized (this) {
                    if (s.getEstimatedFinishTime() >= bestSchedule.getEstimatedFinishTime()) {
                        break;
                    }
                    if (s.getNumberOfTasks() == tasks.length && s.getEstimatedFinishTime() < bestSchedule.getEstimatedFinishTime()) {
                        Entrypoint.stopTimerLabel();
                        bestSchedule = s;
                    }
                }

                expandSchedule(s, schedules);
            }
            return null;
        }
    }
}
