package com.team7.algorithm;

import com.team7.model.Graph;
import com.team7.model.Schedule;

import java.util.Collection;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class ParallelScheduler extends Scheduler{
    private Schedule sharedOptimalSchedule;

    public ParallelScheduler(Graph g, int numOfProcessors) {
        super(g, numOfProcessors);
    }

    private class ExpandingAction extends RecursiveAction{
        private Schedule s;

        public ExpandingAction(Schedule s){
            this.s = s;
        }

        @Override
        protected void compute() {
            boolean expanded = false;

            // if this is not present, the next one
            if(scheduleQueue.isEmpty()){
                expandSchedule(s);
            }
            // before expanding s, see if queue is empty and create a subtask
            synchronized (ExpandingAction.class){
                if(!scheduleQueue.isEmpty()){
                    Schedule newS = scheduleQueue.poll();
                    ExpandingAction action = new ExpandingAction(newS);
                    ForkJoinTask.invokeAll(action);
                }
            }
            // if complete schedule found, set it to be optimal schedule
            if(s.getNumberOfTasks() == tasks.length){
                sharedOptimalSchedule = s;
            }
            // if optimal schedule is not found, expand s
            if(sharedOptimalSchedule != null && expanded == false){
                expandSchedule(s);
            }
        }
    }

    public static synchronized boolean isScheduleQueueEmpty(){
        return scheduleQueue.isEmpty();
    }

    /**
     *
     * @return schedule either null or a schedule
     */
    public static synchronized Schedule pollSchedule(){
        if(isScheduleQueueEmpty()){
            return scheduleQueue.poll();
        }
        return null;
    }

    /**
     * Generates an optimal schedule using an A* algorithm.
     *
     * @return an optimal schedule
     */
    @Override
    public Schedule findOptimalSchedule() {
        findFeasibleSchedule();
        generateInitialSchedules();

        ForkJoinPool forkJoinPool = new ForkJoinPool(3);
//        exit only if all possible expansions are completed and scheduleQueue size is 0
        while(scheduleQueue.size() != 0 || forkJoinPool.hasQueuedSubmissions()){
            Schedule initial = scheduleQueue.poll();
            ExpandingAction expandingAction = new ExpandingAction(initial);
            forkJoinPool.invoke(expandingAction);
        }

        if(sharedOptimalSchedule != null){
            return sharedOptimalSchedule;
        }

        System.out.println("optimal schedule not found");
        return feasibleSchedule;
    }

}
