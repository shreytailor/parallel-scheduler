package com.team7;

import com.team7.algorithm.ParallelSchedulerShareEachLoop;
import com.team7.algorithm.Scheduler;
import com.team7.model.Graph;
import com.team7.model.Schedule;
import com.team7.parsing.DOTParser;
import com.team7.testutil.GraphInfoUtil;
import com.team7.testutil.TaskSchedulingConstraintsChecker;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SchedulerTestCrawledExamples {
    public static final String DOT_TEST_FILE_DIRECTORY = "src/crawled-dot-tests";

    @TestFactory
    Collection<DynamicTest> dynamicTestsWithCollection() {
        List<DynamicTest> tests = new ArrayList<>();

        File directory = new File(DOT_TEST_FILE_DIRECTORY);
        for (File file : directory.listFiles()) {
            if (shouldBeSkipped(file)) {
                continue;
            }
            tests.add(
                    DynamicTest.dynamicTest(
                            file.getName(),
                            () -> testAStarWithDotFile(file)
                    ));
        }

        return tests;
    }

    private void testAStarWithDotFile(File file) {
        // given
        try {
            Graph g = DOTParser.read(file.toString());
            GraphInfoUtil.GraphInfo graphInfo = GraphInfoUtil.getGraphInfo(file.toString());
            System.out.println("graphInfo = " + graphInfo);
            // when
            Scheduler scheduler = new ParallelSchedulerShareEachLoop(g, graphInfo.numberOfTargetProcessors);
            Schedule result = scheduler.findOptimalSchedule();

            // then
            if (shouldBeNullSchedule(file)) {
                assertNull(result);
            } else {
                assertTrue(TaskSchedulingConstraintsChecker.isProcessorConstraintMet(result, g, graphInfo.numberOfTargetProcessors));
                assertTrue(TaskSchedulingConstraintsChecker.isPrecedenceConstraintMet(result, g.getEdges()));
                assertEquals(graphInfo.totalScheduleLength,result.getEstimatedFinishTime());
            }

            System.out.println("schedule = " + result);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }


    private boolean shouldBeNullSchedule(File file) {
        String fileName = file.getName();

        return fileName.contains("cycle") || fileName.contains("empty");
    }


    private boolean shouldBeSkipped(File file) {
        String fileName = file.getName();

        return fileName.contains("cycle") || fileName.contains("empty") || fileName.contains("large");
    }


}
