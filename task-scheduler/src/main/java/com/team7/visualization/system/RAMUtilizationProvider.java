package com.team7.visualization.system;

import javafx.scene.chart.LineChart;

/**
 * This class extends the abstract UtilizationProvider class, and it supplies the RAM Utilization
 * chart with the required information.
 */
public class RAMUtilizationProvider extends UtilizationProvider {

    /**
     * The default constructor of this class which calls the constructor of the parent class.
     * @param chart the chart on which the usage information will be shown.
     */
    public RAMUtilizationProvider(LineChart<String, Number> chart, String title, TimeProvider tp) {
        super(chart, title, tp);
    }

    /**
     * This method gets the current RAM usage of the computer.
     * @return double the amount of RAM which is currently used on the system.
     */
    @Override
    public double getData() {
        return memoryToGb(super.bean.getTotalPhysicalMemorySize() - super.bean.getFreePhysicalMemorySize());
    }

    /**
     * This method gets the upper bound of the chart's Y axis, which is the maximum RAM value.
     * @return double this number symbolises the upper bound of the chart's Y axis.
     */
    @Override
    public double getUpperBound() {
        return memoryToGb(super.bean.getTotalPhysicalMemorySize());
    }

    /**
     * This method is used to convert memory from bytes to gigabytes.
     * @param memory the memory in bytes.
     * @return double the memory in gigabytes.
     */
    public double memoryToGb(double memory) {
        return memory / Math.pow(1024, 3);
    }
}
