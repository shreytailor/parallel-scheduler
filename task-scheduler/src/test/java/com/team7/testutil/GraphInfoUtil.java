package com.team7.testutil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class GraphInfoUtil {

    public static GraphInfo getGraphInfo(String fileName) {
        File file = new File(fileName);

        String infoString = getGraphInfoString(file);

        String[] split = infoString.split(",");

        int[] info = new int[3];

        for (String s : split) {
            if(s.contains("NumberOfTasks")){
                s = s.replaceAll("\\D+","");
                info[0] = Integer.parseInt(s);
            }
            if (s.contains("Totalschedulelength")) {
                s = s.replaceAll("\\D+","");
                info[1] = Integer.parseInt(s);
            }
            if(s.contains("TargetSystem")){
                if(s.contains("Homogeneous")){
                    s = s.replaceAll("\\D+","");
                    info[2] = Integer.parseInt(s);
                }
            }
        }

        GraphInfo graphInfo = new GraphInfo(info[0], info[1], info[2]);

        return graphInfo;
    }

    private static String getGraphInfoString(File file) {
        StringBuilder sb = new StringBuilder();
        try (Scanner scanner = new Scanner(file)) {
            boolean firstBracketFound = false;

            while (scanner.hasNext()) {
                String line = scanner.next();

                if (firstBracketFound) {
                    sb.append(line);
                    if (line.contains("]")) {
                        break;
                    }
                }
                if (line.contains("[")) {
                    firstBracketFound = true;
                    sb.append(line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    public static class GraphInfo{
        public int numberOfTasks; // number
        public int totalScheduleLength; // optimal time
        public int numberOfTargetProcessors;

        public GraphInfo(int numberOfTasks, int totalScheduleLength, int numberOfTargetProcessors) {
            this.numberOfTasks = numberOfTasks;
            this.totalScheduleLength = totalScheduleLength;
            this.numberOfTargetProcessors = numberOfTargetProcessors;
        }

        @Override
        public String toString() {
            return "GraphInfo{" +
                    "numberOfTasks=" + numberOfTasks +
                    ", totalScheduleLength=" + totalScheduleLength +
                    ", numberOfTargetProcessors=" + numberOfTargetProcessors +
                    '}';
        }
    }
}
