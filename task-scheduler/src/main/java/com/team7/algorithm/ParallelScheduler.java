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
            expandSchedule(schedule);
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

    /**
     * Expands schedule s and adds the new schedules to the queue.
     *
     * @param s         the schedule we want to expand
     * @param schedules the queue which we will add new schedules to
     */
    private void expandSchedule(Schedule s, TreeSet<Schedule> schedules) {
        Set<Task> equivalent = new HashSet<>();
        for (Integer t : s.getBeginnableTasks()) {
            if (equivalent.contains(tasks[t])) {
                continue;
            }
            equivalent.addAll(taskEquivalencesMap[t]);

            boolean partiallyExpanded = false;
            boolean normalised = false;
            for (int i = 0; i < processors; i++) {
                int earliestStartTime = calculateEarliestTimeToSchedule(s, tasks[t], i);
                //Normalising
                if (earliestStartTime == 0) {
                    if (normalised) {
                        continue;
                    }
                    normalised = true;
                    if (t < s.getNormalisationIndex()) {
                        continue;
                    }
                }
                Schedule newSchedule = generateNewSchedule(s, tasks[t], i, earliestStartTime);
                sharedState = newSchedule;
                //Only add the new schedule to the queue if it can potentially be better than the feasible schedule.
                if (newSchedule.getEstimatedFinishTime() < feasibleSchedule.getEstimatedFinishTime() &&
                        !containsEquivalentSchedule(newSchedule, tasks[t]) &&
                        !visitedSchedules.contains(newSchedule) &&
                        !schedules.contains(newSchedule)) {
                    schedules.add(newSchedule);
                    if (newSchedule.getEstimatedFinishTime() == s.getEstimatedFinishTime()) {
                        partiallyExpanded = true;
                    }
                }
            }
            if (partiallyExpanded) {
                s.setPartialExpansionIndex(t.byteValue());
                schedules.add(s);
                return;
            }
        }
        synchronized (this) {
            visitedSchedules.add(s);
        }
    }
}
