package com.team7;

import com.team7.model.Edge;
import com.team7.model.Graph;
import com.team7.model.Schedule;
import com.team7.model.Task;
import com.team7.parsing.DOTParser;
import com.team7.testutil.TaskSchedulingConstraintsChecker;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


class SchedulerTest {

    public static final String DOT_TEST_FILE_DIRECTORY = "src/dot-tests";

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
            int numProcessors = 2;
            Scheduler scheduler = new Scheduler(g, numProcessors);
            Schedule result = scheduler.findOptimalSchedule();

            // then
            if (shouldBeNullSchedule(file)) {
                assertNull(result);
            } else {
                assertTrue(TaskSchedulingConstraintsChecker.isProcessorConstraintMet(result, g, numProcessors));
                assertTrue(TaskSchedulingConstraintsChecker.isPrecedenceConstraintMet(result, g.getEdges()));
            }

            System.out.println("schedule = " + result);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    private boolean shouldBeSkipped(File file) {
        String fileName = file.getName();

        return fileName.contains("cycle") || fileName.contains("empty") || fileName.contains("large");
    }
    private boolean shouldBeNullSchedule(File file) {
        String fileName = file.getName();

        return fileName.contains("cycle") || fileName.contains("empty");
    }
}