package com.team7;

import com.team7.algorithm.ParallelScheduler;
import com.team7.algorithm.Scheduler;
import com.team7.model.Graph;
import com.team7.model.Schedule;
import com.team7.parsing.DOTParser;
import com.team7.testutil.GraphInfoUtil;
import com.team7.testutil.TaskSchedulingConstraintsChecker;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SchedulerTestCrawledExamples {
    public static final String DOT_TEST_FILE_DIRECTORY = "src/crawled-dot-tests";

    @TestFactory
    Collection<DynamicTest> dynamicTestsWithCollection() {
        List<DynamicTest> tests = new ArrayList<>();
        Entrypoint.IS_TEST_MODE = true;

        //only test ones that allocates to 2, 4 processors, and has less than 20 nodes
        File directory = new File(DOT_TEST_FILE_DIRECTORY);
        for (File file : directory.listFiles()) {
            GraphInfoUtil.GraphInfo graphInfo = GraphInfoUtil.getGraphInfo(file.toString());

            boolean numProcessorsTwoOrFour = graphInfo.numberOfTargetProcessors == 2 || graphInfo.numberOfTargetProcessors == 4|| graphInfo.numberOfTargetProcessors == 6;
            boolean numNodesLessThanTwenty = graphInfo.numberOfTasks<16;

            if(numProcessorsTwoOrFour && numNodesLessThanTwenty){
                tests.add(
                        DynamicTest.dynamicTest(
                                file.getName(),
                                () -> testAStarWithDotFile(file, graphInfo)
                        ));
            }
        }

        return tests;
    }

    private void testAStarWithDotFile(File file, GraphInfoUtil.GraphInfo graphInfo) {
        // given
        try {
            Graph g = DOTParser.read(file.toString());
            // when
            // ignore case where it's not homogeneous - that is, when target system number of processors is parsed to be 0
            if(graphInfo.numberOfTargetProcessors == 0){
                fail("ignore this case");
            }

            Scheduler scheduler = new Scheduler(g, graphInfo.numberOfTargetProcessors);
            assertTimeout(Duration.ofSeconds(120), ()->{
                Schedule result = scheduler.findOptimalSchedule();
                assertTrue(TaskSchedulingConstraintsChecker.isProcessorConstraintMet(result, g, graphInfo.numberOfTargetProcessors));
                assertTrue(TaskSchedulingConstraintsChecker.isPrecedenceConstraintMet(result, g.getEdges()));
                assertEquals(graphInfo.totalScheduleLength,result.getEstimatedFinishTime());
            });
            // then

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}
