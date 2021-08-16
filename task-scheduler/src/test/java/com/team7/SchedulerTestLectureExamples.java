package com.team7;

import com.team7.algorithm.ParallelScheduler;
import com.team7.algorithm.Scheduler;
import com.team7.model.Graph;
import com.team7.model.Schedule;
import com.team7.parsing.DOTParser;
import com.team7.testutil.TaskSchedulingConstraintsChecker;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SchedulerTestLectureExamples {
    public static final String DOT_TEST_FILE_DIRECTORY = "src/lecture-dot-tests";
    public static final int NUM_PROCESSORS = 2;

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
            // when

            Scheduler scheduler = new ParallelScheduler(g, NUM_PROCESSORS);
            Schedule result = scheduler.findOptimalSchedule();

            // then
            if (shouldBeNullSchedule(file)) {
                assertNull(result);
            } else {
                assertTrue(TaskSchedulingConstraintsChecker.isProcessorConstraintMet(result, g, NUM_PROCESSORS));
                assertTrue(TaskSchedulingConstraintsChecker.isPrecedenceConstraintMet(result, g.getEdges()));
                match(result.getEstimatedFinishTime(), file, NUM_PROCESSORS);
            }

            System.out.println("schedule = " + result);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }


    private void match(int actualFinishTime, File file, int no_processor) {
        if (no_processor != 2 && no_processor != 4) {
            throw new Error("Not a valid number of processors: " + no_processor);
        }

        int expectedFinishTime = -1;
        String fileName = file.getName();

        if (no_processor == 2) {
            expectedFinishTime =
                    fileName.contains("Nodes_7_OutTree") ? 28 :
                            fileName.contains("Nodes_8_Random") ? 581 :
                                    fileName.contains("Nodes_9_SeriesParallel") ? 55 :
                                            fileName.contains("Nodes_10_Random") ? 50 :
                                                    fileName.contains("Nodes_11_OutTree") ? 350 : -1;
        } else if (no_processor == 4) {
            expectedFinishTime =
                    fileName.contains("Nodes_7_OutTree") ? 22 :
                            fileName.contains("Nodes_8_Random") ? 581 :
                                    fileName.contains("Nodes_9_SeriesParallel") ? 55 :
                                            fileName.contains("Nodes_10_Random") ? 50 :
                                                    fileName.contains("Nodes_11_OutTree") ? 227 : -1;
        }

        if (expectedFinishTime == -1) {
            throw new Error("This is not a file given by Oliver Sinnen: " + file.getName());
        }
        
        assertEquals(expectedFinishTime, actualFinishTime);
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
