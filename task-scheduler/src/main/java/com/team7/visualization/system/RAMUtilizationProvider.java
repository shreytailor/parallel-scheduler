package com.team7.visualization.system;

import javafx.scene.chart.LineChart;

public class RAMUtilizationProvider extends UtilizationProvider {

    public RAMUtilizationProvider(LineChart<String, Number> chart) {
        super(chart);
    }

    @Override
    public double getData() {
        return memoryToGb(super.bean.getTotalPhysicalMemorySize() - super.bean.getFreePhysicalMemorySize());
    }

    @Override
    public double getUpperBound() {
        return memoryToGb(super.bean.getTotalPhysicalMemorySize());
    }

    public double memoryToGb(double memory) {
        System.out.println(memory / Math.pow(1024, 3));
//        return (int) (memory / Math.pow(1024, 3));
        return memory / Math.pow(1024, 3);
    }
}
